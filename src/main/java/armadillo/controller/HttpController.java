package armadillo.controller;

import armadillo.mapper.*;
import armadillo.model.*;
import armadillo.result.ApiResponse;
import armadillo.result.Other;
import armadillo.result.sys.ChartInfo;
import armadillo.result.sys.Softs;
import armadillo.utils.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HTTP REST API控制器
 * 复用SocketController中的业务逻辑，使用MyBatis进行数据库操作
 */
@RestController
@RequestMapping("/api")
public class HttpController {

    private static final Logger logger = Logger.getLogger(HttpController.class);

    // ==================== 用户相关接口 ====================

    /**
     * 账号登录
     * 对应SocketController case 2003
     */
    @PostMapping("/user/login")
    public ApiResponse login(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            String username = params.getOrDefault("username", "").toString().replaceAll("\\s+", "");
            String password = params.getOrDefault("password", "").toString().replaceAll("\\s+", "");

            if (username.isEmpty() || password.isEmpty()) {
                return ApiResponse.error("用户名和密码不能为空");
            }

            SysUser sysUser = sysUserMapper.findUserName(username);
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            if (!sysUser.getPassword().equals(Md5Utils.md5(password.getBytes()))) {
                return ApiResponse.error(404, "密码错误");
            }

            sysUser.setLoginCount(sysUser.getLoginCount() == null ? 1 : sysUser.getLoginCount() + 1);
            sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getUsername() + sysUser.getPassword()));
            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1) {
                return ApiResponse.success(sysUser);
            } else {
                return ApiResponse.error(500, "登录失败，请重试");
            }
        } catch (Exception e) {
            logger.error("登录异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 账号注册
     * 对应SocketController case 2004
     */
    @PostMapping("/user/register")
    public ApiResponse register(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            String username = params.getOrDefault("username", "").toString().replaceAll("\\s+", "");
            String password = params.getOrDefault("password", "").toString().replaceAll("\\s+", "");
            String email = params.getOrDefault("email", "").toString();

            if (username.isEmpty() || password.isEmpty()) {
                return ApiResponse.error("用户名和密码不能为空");
            }

            SysUser sysUser = sysUserMapper.findUserName(username);
            if (sysUser != null) {
                return ApiResponse.error(404, "用户名已存在");
            }

            SysUser sysUser2 = sysUserMapper.findEmail(email);
            if (sysUser2 != null) {
                return ApiResponse.error(404, "邮箱已被注册");
            }

            sysUser = new SysUser();
            sysUser.setUsername(username);
            sysUser.setPassword(Md5Utils.md5(password.getBytes()));
            sysUser.setEmail(email);
            sysUser.setLoginCount(0);
            sysUser.setExpireTime(new Date(System.currentTimeMillis()));
            sysUser.setRegTime(new Date(System.currentTimeMillis()));
            sysUser.setValue(0);
            sysUser.setOpenid("");
            sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getUsername() + sysUser.getPassword()));

            if (sysUserMapper.insert(sysUser) == 1) {
                return ApiResponse.success("注册成功", null);
            } else {
                return ApiResponse.error(500, "注册失败，请重试");
            }
        } catch (Exception e) {
            logger.error("注册异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取用户信息
     * 对应SocketController case 2001
     */
    @GetMapping("/user/info")
    public ApiResponse getUserInfo(@RequestParam String token) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            SysUser sysUser = sysUserMapper.findTokenUser(token.replaceAll("\\s+", ""));

            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            sysUser.setLoginCount(sysUser.getLoginCount() + 1);
            sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getOpenid()));
            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1) {
                return ApiResponse.success(sysUser);
            } else {
                return ApiResponse.error(500, "获取用户信息失败");
            }
        } catch (Exception e) {
            logger.error("获取用户信息异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 修改密码
     * 对应SocketController case 2006
     */
    @PostMapping("/user/change-password")
    public ApiResponse changePassword(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            String token = params.getOrDefault("token", "").toString().replaceAll("\\s+", "");
            String oldPassword = params.getOrDefault("password", "").toString();
            String newPassword = params.getOrDefault("newpassword", "").toString();

            if (token.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                return ApiResponse.error("参数不完整");
            }

            String srcPass = Md5Utils.md5(oldPassword.getBytes());
            String newPass = Md5Utils.md5(newPassword.getBytes());

            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            SysUser sysUser = sysUserMapper.findTokenUser(token);

            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            if (sysUser.getPassword() == null || sysUser.getPassword().isEmpty()) {
                return ApiResponse.error(404, "当前账号未设置密码");
            }

            if (!sysUser.getPassword().equals(srcPass)) {
                return ApiResponse.error(404, "原密码错误");
            }

            sysUser.setPassword(newPass);
            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1) {
                return ApiResponse.success("密码修改成功", null);
            } else {
                return ApiResponse.error(500, "密码修改失败");
            }
        } catch (Exception e) {
            logger.error("修改密码异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 卡密充值
     * 对应SocketController case 2002
     */
    @PostMapping("/user/recharge")
    public ApiResponse recharge(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            SysCardMapper sysCardMapper = sqlSession.getMapper(SysCardMapper.class);

            String token = params.getOrDefault("token", "").toString().replaceAll("\\s+", "");
            String card = params.getOrDefault("card", "").toString().replaceAll("\\s+", "");

            if (token.isEmpty() || card.isEmpty()) {
                return ApiResponse.error("参数不完整");
            }

            SysUser sysUser = sysUserMapper.findTokenUser(token);
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            SysCard sysCard = sysCardMapper.findCard(card);
            if (sysCard == null) {
                return ApiResponse.error(404, "卡密不存在");
            }

            if (!sysCard.getUsable()) {
                return ApiResponse.error(404, "卡密已被使用");
            }

            // 计算卡密时间
            long time = Math.max(sysUser.getExpireTime().getTime(), System.currentTimeMillis());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
            switch (sysCard.getType()) {
                case 1: // 月卡
                    localDateTime = localDateTime.plusMonths(sysCard.getCount());
                    sysUser.setValue(sysUser.getValue() + sysCard.getCount() * 100000);
                    break;
                case 2: // 年卡
                    localDateTime = localDateTime.plusYears(sysCard.getCount());
                    sysUser.setValue(sysUser.getValue() + sysCard.getCount() * 100000);
                    break;
                case 3: // 永久VIP试用卡
                    localDateTime = localDateTime.plusDays(sysCard.getCount());
                    sysUser.setValue(sysUser.getValue() + 100000);
                    break;
                default:
                    return ApiResponse.error("无效的卡密类型");
            }

            ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
            sysUser.setExpireTime(Date.from(zdt.toInstant()));

            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1) {
                sysCard.setUserId(sysUser.getId());
                sysCard.setUsable(false);
                sysCard.setUsrTime(new Date(System.currentTimeMillis()));
                if (sysCardMapper.updateByPrimaryKey(sysCard) == 1) {
                    return ApiResponse.success("充值成功", sysUser);
                } else {
                    return ApiResponse.error(500, "充值失败，请重试");
                }
            } else {
                return ApiResponse.error(500, "充值失败，请重试");
            }
        } catch (Exception e) {
            logger.error("充值异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 应用相关接口 ====================

    /**
     * 获取应用列表
     * 对应SocketController case 3000
     */
    @GetMapping("/soft/list")
    public ApiResponse getSoftList(@RequestParam String token,
                                   @RequestParam(defaultValue = "0") int offset,
                                   @RequestParam(defaultValue = "10") int limit) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);

            SysUser sysUser = sysUserMapper.findTokenUser(token.replaceAll("\\s+", ""));
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            String min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(java.time.format.DateTimeFormatter.ofPattern("MM/dd"));
            List<Softs> softs = userSoftMapper.findOffSetSofts(sysUser.getId(), offset, limit);

            for (Softs soft : softs) {
                // 获取6天的数据统计
                List<ChartInfo> chartInfos = new ArrayList<>();
                LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(5);
                for (int i = 0; i < 6; i++) {
                    LocalDateTime start = todayStart.plusDays(i);
                    String day = start.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd"));
                    int usrCount = 0;
                    int startCount = 0;
                    if (redisUtil.exists(soft.getAppkey() + "-count-" + day))
                        usrCount = redisUtil.scard(soft.getAppkey() + "-count-" + day).intValue();
                    if (redisUtil.exists(soft.getAppkey() + "-start-" + day))
                        startCount = Integer.parseInt(redisUtil.get(soft.getAppkey() + "-start-" + day));
                    chartInfos.add(new ChartInfo(day, usrCount, startCount));
                }
                soft.setChartInfos(chartInfos);

                // 获取今日活跃用户
                if (redisUtil.exists(soft.getAppkey() + "-count-" + min))
                    soft.setTotal_user(redisUtil.scard(soft.getAppkey() + "-count-" + min).intValue());
            }

            return ApiResponse.success(softs);
        } catch (Exception e) {
            logger.error("获取应用列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取应用详情
     * 对应SocketController case 3002
     */
    @GetMapping("/soft/detail")
    public ApiResponse getSoftDetail(@RequestParam String token,
                                     @RequestParam String key,
                                     @RequestParam int flag) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
            SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
            SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
            SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
            SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);

            SysUser sysUser = sysUserMapper.findTokenUser(token.replaceAll("\\s+", ""));
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (flag == 0) {
                return ApiResponse.error("无效的模块标志");
            }

            armadillo.enums.SoftEnums softFlag = armadillo.enums.SoftEnums.getFlags(flag)[0];
            Object data;
            switch (softFlag) {
                case Notice:
                    data = remoteNoticeMapper.selectBySoftId(soft.getId());
                    break;
                case SingleVerify:
                    data = singleVerifyMapper.selectBySoftId(soft.getId());
                    break;
                case Update:
                    data = softUpdateMapper.selectBySoftId(soft.getId());
                    break;
                case CustomModule:
                    data = softCustomMapper.selectBySoftId(soft.getId());
                    break;
                case Admob:
                    data = softAdmobMapper.selectBySoftId(soft.getId());
                    break;
                default:
                    return ApiResponse.error("不支持的模块类型");
            }

            return ApiResponse.success(data);
        } catch (Exception e) {
            logger.error("获取应用详情异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 保存应用配置
     * 对应SocketController case 3003
     */
    @PostMapping("/soft/save")
    public ApiResponse saveSoftConfig(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
            SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
            SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
            SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
            SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);

            String token = params.getOrDefault("token", "").toString().replaceAll("\\s+", "");
            String key = params.getOrDefault("key", "").toString();
            int flag = params.getOrDefault("flag", 0) instanceof Number ? ((Number) params.get("flag")).intValue() : 0;
            String info = params.getOrDefault("info", "").toString();

            if (token.isEmpty() || key.isEmpty() || flag == 0 || info.isEmpty()) {
                return ApiResponse.error("参数不完整");
            }

            SysUser sysUser = sysUserMapper.findTokenUser(token);
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (soft.getUserId().intValue() != sysUser.getId().intValue()) {
                return ApiResponse.error(403, "无权操作此应用");
            }

            armadillo.enums.SoftEnums softFlag = armadillo.enums.SoftEnums.getFlags(flag)[0];
            String decodedInfo = new String(Base64.getDecoder().decode(info.getBytes()), java.nio.charset.StandardCharsets.UTF_8);
            com.google.gson.Gson gson = new com.google.gson.Gson();

            switch (softFlag) {
                case Notice: {
                    RemoteNotice newNotice = gson.fromJson(decodedInfo, RemoteNotice.class);
                    RemoteNotice remoteNotice = remoteNoticeMapper.selectBySoftId(soft.getId());
                    if (remoteNotice == null) {
                        newNotice.setSoftId(soft.getId());
                        if (remoteNoticeMapper.insert(newNotice) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    } else {
                        newNotice.setSoftId(soft.getId());
                        newNotice.setCid(remoteNotice.getCid());
                        if (remoteNoticeMapper.updateByPrimaryKey(newNotice) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    }
                }
                case SingleVerify: {
                    SingleVerify newSingle = gson.fromJson(decodedInfo, SingleVerify.class);
                    SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                    if (singleVerify == null) {
                        newSingle.setSoftId(soft.getId());
                        if (singleVerifyMapper.insert(newSingle) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    } else {
                        newSingle.setSoftId(soft.getId());
                        newSingle.setCid(singleVerify.getCid());
                        if (singleVerifyMapper.updateByPrimaryKey(newSingle) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    }
                }
                case Update: {
                    SoftUpdate newUpdate = gson.fromJson(decodedInfo, SoftUpdate.class);
                    SoftUpdate softUpdate = softUpdateMapper.selectBySoftId(soft.getId());
                    if (softUpdate == null) {
                        newUpdate.setSoftId(soft.getId());
                        if (softUpdateMapper.insert(newUpdate) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    } else {
                        newUpdate.setSoftId(soft.getId());
                        newUpdate.setCid(softUpdate.getCid());
                        if (softUpdateMapper.updateByPrimaryKey(newUpdate) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    }
                }
                case CustomModule: {
                    SoftCustom newCustom = gson.fromJson(decodedInfo, SoftCustom.class);
                    SoftCustom softCustom = softCustomMapper.selectBySoftId(soft.getId());
                    if (softCustom == null) {
                        newCustom.setSoftId(soft.getId());
                        if (softCustomMapper.insert(newCustom) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    } else {
                        newCustom.setSoftId(soft.getId());
                        newCustom.setCid(softCustom.getCid());
                        if (softCustomMapper.updateByPrimaryKey(newCustom) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    }
                }
                case Admob: {
                    SoftAdmob newAdmob = gson.fromJson(decodedInfo, SoftAdmob.class);
                    SoftAdmob softAdmob = softAdmobMapper.selectBySoftId(soft.getId());
                    if (softAdmob == null) {
                        newAdmob.setSoftId(soft.getId());
                        if (softAdmobMapper.insert(newAdmob) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    } else {
                        newAdmob.setSoftId(soft.getId());
                        newAdmob.setCid(softAdmob.getCid());
                        if (softAdmobMapper.updateByPrimaryKey(newAdmob) == 1)
                            return ApiResponse.success("保存成功", null);
                        else
                            return ApiResponse.error(500, "保存失败");
                    }
                }
                default:
                    return ApiResponse.error("不支持的模块类型");
            }
        } catch (Exception e) {
            logger.error("保存应用配置异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 删除应用
     * 对应SocketController case 3100
     */
    @DeleteMapping("/soft/delete")
    public ApiResponse deleteSoft(@RequestParam String token, @RequestParam String key) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
            SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
            SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
            SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
            SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
            SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);

            SysUser sysUser = sysUserMapper.findTokenUser(token.replaceAll("\\s+", ""));
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (soft.getUserId().intValue() != sysUser.getId().intValue()) {
                return ApiResponse.error(403, "无权操作此应用");
            }

            if (userSoftMapper.deleteByPrimaryKey(soft.getId()) == 1) {
                remoteNoticeMapper.deleteBySoftId(soft.getId());
                singleVerifyMapper.deleteBySoftId(soft.getId());
                singleCardMapper.deleteBySoftId(soft.getId());
                singleTrialMapper.deleteBySoftId(soft.getId());
                softUpdateMapper.deleteBySoftId(soft.getId());
                softCustomMapper.deleteBySoftId(soft.getId());
                softAdmobMapper.deleteBySoftId(soft.getId());
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除应用异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 卡密相关接口 ====================

    /**
     * 获取卡密列表
     * 对应SocketController case 3004 flag=2
     */
    @GetMapping("/card/list")
    public ApiResponse getCardList(@RequestParam String token,
                                   @RequestParam String key,
                                   @RequestParam(defaultValue = "0") int offset,
                                   @RequestParam(defaultValue = "10") int limit) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);

            SysUser sysUser = sysUserMapper.findTokenUser(token.replaceAll("\\s+", ""));
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (soft.getUserId().intValue() != sysUser.getId().intValue()) {
                return ApiResponse.error(403, "无权操作此应用");
            }

            List<SingleCard> cards = singleCardMapper.findOffSetCards(soft.getId(), offset, limit);
            return ApiResponse.success(cards);
        } catch (Exception e) {
            logger.error("获取卡密列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 生成卡密
     * 对应SocketController case 3004 flag=1
     */
    @PostMapping("/card/generate")
    public ApiResponse generateCards(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);

            String token = params.getOrDefault("token", "").toString().replaceAll("\\s+", "");
            String key = params.getOrDefault("key", "").toString();
            int count = params.getOrDefault("count", 0) instanceof Number ? ((Number) params.get("count")).intValue() : 0;
            int type = params.getOrDefault("type", 0) instanceof Number ? ((Number) params.get("type")).intValue() : 0;
            int value = params.getOrDefault("value", 0) instanceof Number ? ((Number) params.get("value")).intValue() : 0;
            String mark = params.getOrDefault("mark", "").toString();

            if (token.isEmpty() || key.isEmpty()) {
                return ApiResponse.error("参数不完整");
            }

            SysUser sysUser = sysUserMapper.findTokenUser(token);
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            if (sysUser.getExpireTime().getTime() < System.currentTimeMillis()) {
                return ApiResponse.error(403, "账号已过期，无法生成卡密");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (soft.getUserId().intValue() != sysUser.getId().intValue()) {
                return ApiResponse.error(403, "无权操作此应用");
            }

            if (count > 99 || value > 99) {
                return ApiResponse.error("数量或面值超出限制");
            }

            List<SingleCard> singleCards = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                SingleCard singleCard = new SingleCard();
                singleCard.setCard(CardRadom.radomCard());
                singleCard.setType(type);
                singleCard.setValue(value);
                singleCard.setMark(mark);
                singleCard.setSoftId(soft.getId());
                singleCards.add(singleCard);
            }
            singleCardMapper.insertAll(singleCards);
            return ApiResponse.success("生成成功", singleCards);
        } catch (Exception e) {
            logger.error("生成卡密异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 删除卡密
     * 对应SocketController case 3004 flag=3
     */
    @DeleteMapping("/card/delete")
    public ApiResponse deleteCards(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);

            String token = params.getOrDefault("token", "").toString().replaceAll("\\s+", "");
            String key = params.getOrDefault("key", "").toString();

            @SuppressWarnings("unchecked")
            List<String> cardList = (List<String>) params.get("cards");

            if (token.isEmpty() || key.isEmpty() || cardList == null || cardList.isEmpty()) {
                return ApiResponse.error("参数不完整");
            }

            SysUser sysUser = sysUserMapper.findTokenUser(token);
            if (sysUser == null) {
                return ApiResponse.error(404, "用户不存在或Token无效");
            }

            UserSoft soft = userSoftMapper.findSoftKey(key);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            if (soft.getUserId().intValue() != sysUser.getId().intValue()) {
                return ApiResponse.error(403, "无权操作此应用");
            }

            for (String card : cardList) {
                singleCardMapper.deleteByCard(card, soft.getId());
            }
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            logger.error("删除卡密异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 任务相关接口 ====================

    /**
     * 获取任务信息
     * 对应SocketController case 9000
     */
    @GetMapping("/task/info")
    public ApiResponse getTaskInfo(@RequestParam String uuid) {
        try {
            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            if (redisUtil.exists(uuid)) {
                return ApiResponse.success(com.alibaba.fastjson.JSONObject.parse(redisUtil.get(uuid)));
            } else {
                return ApiResponse.error(404, "任务不存在或已过期");
            }
        } catch (Exception e) {
            logger.error("获取任务信息异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 终止任务
     * 对应SocketController case 9002
     */
    @PostMapping("/task/stop")
    public ApiResponse stopTask(@RequestBody Map<String, Object> params) {
        try {
            String uuid = params.getOrDefault("uuid", "").toString();
            if (uuid.isEmpty()) {
                return ApiResponse.error("任务ID不能为空");
            }

            java.io.File task = new java.io.File(armadillo.Constant.getTask(), uuid);
            if (task.exists()) {
                task.delete();
            }

            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            if (redisUtil.exists(uuid)) {
                redisUtil.del(uuid);
            }

            if (armadillo.Constant.getTask_map().containsKey(uuid)) {
                armadillo.action.TaskAction runnable = (armadillo.action.TaskAction) armadillo.Constant.getTask_map().get(uuid);
                runnable.Cancel();
            }

            return ApiResponse.success("任务已终止", null);
        } catch (Exception e) {
            logger.error("终止任务异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 系统相关接口 ====================

    /**
     * 获取公告列表
     * 对应SocketController case 1001
     */
    @GetMapping("/system/notices")
    public ApiResponse getNotices() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);
            SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);

            List<SysNotice> notices = new ArrayList<>();
            for (SysVer sysVer : sysVerMapper.selectAll()) {
                SysNotice notice = new SysNotice();
                notice.setTitle(sysVer.getVersionName());
                notice.setMsg(sysVer.getVersionMsg());
                notice.setTime(sysVer.getTime());
                notices.add(notice);
            }
            notices.addAll(sysNoticeMapper.selectAll());
            notices.sort((o1, o2) -> {
                if (o1.getTime().before(o2.getTime()))
                    return 1;
                return -1;
            });

            return ApiResponse.success(notices);
        } catch (Exception e) {
            logger.error("获取公告列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取版本列表
     * 对应SocketController case 1002
     */
    @GetMapping("/system/versions")
    public ApiResponse getVersions(@RequestParam(required = false, defaultValue = "0") int version) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);
            List<SysVer> versions = sysVerMapper.findNewVer(version);

            if (versions.size() > 0) {
                return ApiResponse.success(versions);
            } else {
                return ApiResponse.success("暂无新版本", null);
            }
        } catch (Exception e) {
            logger.error("获取版本列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }
}
