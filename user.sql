/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80024 (8.0.24)
 Source Host           : localhost:3306
 Source Schema         : play

 Target Server Type    : MySQL
 Target Server Version : 80024 (8.0.24)
 File Encoding         : 65001

 Date: 08/12/2025 08:30:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
  `user_account` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `create_date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建时间',
  `update_date` datetime NOT NULL COMMENT '更新时间',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `telephone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `avater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  `birthday` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出生年月日',
  `sex` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别，0:男，1:女',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个性签名',
  `region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地区',
  `disabled` int NULL DEFAULT 0 COMMENT '是否禁用，0表示不不禁用，1表示禁用',
  `permission` int NULL DEFAULT 0 COMMENT '权限大小'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('ddc0c4433236405695c8e6f2c6fd959b', '且听风铃', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:54:22', '2024-01-21 10:56:13', '且听风铃2', '15302686947', '275018723@qq.com', '/static/user/avater/且听风铃.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('cd42a78a1dbf4c81ace10aa7c3745417', '初晓微芒', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:47:01', '2020-01-11 18:47:03', '初晓微芒', '15302686947', '275018723@qq.com', '/static/user/avater/初晓微芒.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('ab987cf6cc0f43db804f30224c4a11b3', '半夏时光', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:46:09', '2020-01-11 18:46:12', '半夏时光', '15302686947', '275018723@qq.com', '/static/user/avater/半夏时光.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('e7d73b8c9c484b7bac237d934fa8ea9d', '半岛弥音', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-18 18:41:52', '2020-01-11 18:41:57', '半岛弥音', '15302686947', '275018723@qq.com', '/static/user/avater/半岛弥音.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('0786f5e8bdc94165bc3e72d9e1e28c6b', '吴尽吴穷', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-19 00:59:28', '2019-08-19 00:59:32', '吴尽吴穷', '15302686947', '275018723@qq.com', '/static/user/avater/吴怨吴悔.jpg', '1990-10-8', '0', 'public', '无穷无尽的爱', NULL, 0, 0);
INSERT INTO `user` VALUES ('aa182b3c1d4f44ecba66cd03f781f727', '吴忧吴虑', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-13 21:01:56', '2019-08-13 21:02:02', '吴忧吴虑', '15302686947', '275018723@qq.com', '/static/user/avater/吴怨吴悔.jpg', '1990-10-8', '0', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('e991bfe7598e4ebeab3dd4af9b7d09b0', '吴怨吴悔', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-13 00:00:00', '2024-07-31 23:15:30', '吴怨吴悔', '15302686947', '275018723@qq.com', '/static/user/avater/吴怨吴悔.jpg', '1990-10-10', '0', 'admin', '无怨，有悔', NULL, 0, 1);
INSERT INTO `user` VALUES ('f71d6c016fa94cd29f9db53f71ec7b62', '吴时吴刻', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-12 00:00:00', '2024-01-19 23:17:29', '吴时吴刻', '15302686947', '275018723@qq.com', '/static/user/avater/吴时吴刻.jpg', '1990-10-8', '0', 'public', '无时无刻不想你', NULL, 0, 0);
INSERT INTO `user` VALUES ('0115717756a14136bc1b462870002d9f', '夕颜泪痕', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:55:53', '2020-01-11 18:55:56', '夕颜泪痕', '15302686947', '275018723@qq.com', '/static/user/avater/夕颜泪痕.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('6d6f45b93ae44f01aae3e115f93034cd', '孤影倾城', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:49:08', '2024-02-25 23:27:01', '孤影倾城', '15302686947', '275018723@qq.com', '/static/user/avater/孤影倾城.jpg', '1990-10-8', '1', 'public', '无怨，有悔', '深圳', 0, 0);
INSERT INTO `user` VALUES ('3387040af5d74ece8414d4ea8b557997', '归去如风', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:49:43', '2020-01-11 18:49:47', '归去如风', '15302686947', '275018723@qq.com', '/static/user/avater/归去如风.jpg', '1990-10-8', '0', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('7ab88f509e6c4c9cb86057c8edf1b893', '灯火阑珊', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:47:39', '2024-02-25 15:42:22', '灯火阑珊2', '15302686947', '275018723@qq.com', '/static/user/avater/灯火阑珊.jpg', '1989-10-08', '1', 'public', '无怨，有悔', '深圳', 0, 0);
INSERT INTO `user` VALUES ('bb609cb99e734b79834364e7d0c474e8', '离殇荡情', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:50:58', '2020-01-11 18:51:01', '离殇荡情', '15302686947', '275018723@qq.com', '/static/user/avater/离殇荡情.jpeg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('783f5f7a16ea4e1ea8c3f5425d056f7d', '秋水天长', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:55:00', '2024-04-21 17:29:57', '秋水天长2', '15302686947', '275018723@qq.com', '/static/user/avater/秋水天长.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('75a3d23fc4904150950a1c7828d6bb06', '空城旧梦', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:50:23', '2023-04-21 23:52:38', '空城旧梦', '15302686947', '275018723@qq.com', '/static/user/avater/空城旧梦.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('06ed9c2f9f3f4555a828a780bd790d92', '落寞雨季', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:53:02', '2022-12-18 00:11:04', '落寞雨季', '15302686947', '275018723@qq.com', '/static/user/avater/落寞雨季.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('7e2f9b4443204bb5a930b395233ab49f', '落落清欢', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:52:15', '2024-04-25 22:40:20', '落落清欢', '15302686947', '275018723@qq.com', '/static/user/avater/落落清欢.jpg', '1990-10-8', '0', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('c1c50bf2beb54489be3d88643598ede1', '逆夏光年', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:53:40', '2020-01-11 18:53:42', '逆夏光年', '15302686947', '275018723@qq.com', '/static/user/avater/逆夏光年.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('19cc5f1a07f3403c9f52371a01876fec', '離別的抽泣', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:51:33', '2024-02-23 21:27:02', '離別的抽泣3', '15302686947', '275018723@qq.com', '/static/user/avater/離別的抽泣.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('a4d9df6c825f48b2a1f95f928c3dc067', '雨晨清风', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:57:53', '2024-01-10 23:16:14', '雨晨清风', '15302686947', '275018723@qq.com', '/static/user/avater/雨晨清风.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);
INSERT INTO `user` VALUES ('d32d601d12494fb3915ebe2705b504f7', '飞颜尘雪', 'e10adc3949ba59abbe56e057f20f883e', '2020-01-11 18:48:28', '2020-01-11 18:48:31', '飞颜尘雪', '15302686947', '275018723@qq.com', '/static/user/avater/飞颜尘雪.jpg', '1990-10-8', '1', 'public', '无怨，有悔', NULL, 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
