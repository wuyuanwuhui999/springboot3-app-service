package com.player.music.utils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 大模型生成的 WHERE 条件校验（只读白名单）。
 * <p>
 * 规则与 FastAPI 项目 common/services/llm_service.py 的 validate_sql_condition 一致：
 * 禁止分号/注释/写操作/危险函数，FROM、JOIN 后面的表必须是音乐相关表。
 */
public final class SqlConditionValidator {

    /** sql_condition 长度上限 */
    private static final int MAX_CONDITION_LENGTH = 2000;

    /** 允许出现的表 */
    private static final List<String> ALLOWED_TABLES = List.of(
            "music", "music_like", "music_favorite_list", "music_category", "music_category_relation");

    /** 禁止出现的关键字（只允许只读查询） */
    private static final List<String> FORBIDDEN_KEYWORDS = List.of(
            "insert", "update", "delete", "drop", "truncate", "alter", "create", "replace",
            "grant", "revoke", "load_file", "outfile", "information_schema", "sleep(", "benchmark(",
            "procedure", "call ", "union select");

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("\\b(?:from|join)\\s+([A-Za-z_][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);

    private SqlConditionValidator() {
    }

    /**
     * 校验查询条件。
     *
     * @param condition 大模型生成的 WHERE 条件
     * @return null 表示通过；否则返回不通过的原因
     */
    public static String validate(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return "查询条件为空";
        }

        String cond = condition.trim();
        if (cond.length() > MAX_CONDITION_LENGTH) {
            return "查询条件过长（>" + MAX_CONDITION_LENGTH + " 字符）";
        }

        // 去掉绑定占位符后再做词法检查
        String probe = cond.replace(":keyword", " ").replace(":userId", " ").replace(":user_id", " ");
        if (probe.contains(";")) {
            return "查询条件里不允许出现分号";
        }
        if (probe.contains("--") || probe.contains("/*") || probe.contains("#")) {
            return "查询条件里不允许出现注释";
        }

        String lowered = probe.toLowerCase(Locale.ROOT);
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lowered.contains(keyword)) {
                return "查询条件里不允许出现 " + keyword.toUpperCase(Locale.ROOT) + " 等写操作/危险函数";
            }
        }

        Matcher matcher = TABLE_PATTERN.matcher(lowered);
        while (matcher.find()) {
            String table = matcher.group(1);
            if (!ALLOWED_TABLES.contains(table) && !table.startsWith("music")) {
                return "查询条件里不允许访问表 " + table;
            }
        }
        return null;
    }

    /**
     * 把大模型可能生成的旧式占位符统一成 MyBatis 的 #{keyword}。
     * <p>
     * 大模型按提示词输出 :keyword / :user_id，这里转换成 MyBatis 参数占位符，
     * 这样条件本身走 ${} 拼接（需先通过 {@link #validate(String)}），值仍走 #{} 预编译绑定。
     */
    public static String toMybatisCondition(String condition) {
        if (condition == null) {
            return "";
        }
        String cond = condition;
        // 旧式占位符（agent 早期版本可能产出）
        cond = cond.replace("'%%s%%'", "#{keyword}").replace("\"%%s%%\"", "#{keyword}").replace("%%s%%", "#{keyword}");
        cond = cond.replace("'%s'", "#{keyword}").replace("\"%s\"", "#{keyword}").replace("%s", "#{keyword}");
        // 标准占位符
        cond = cond.replace(":keyword", "#{keyword}");
        cond = cond.replace(":user_id", "#{userId}");
        cond = cond.replace(":userId", "#{userId}");
        return cond.trim();
    }
}
