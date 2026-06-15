package armadillo.controller;

import armadillo.mapper.*;
import armadillo.model.*;
import armadillo.result.ApiResponse;
import armadillo.utils.MyBatisUtil;
import armadillo.utils.RedisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台API控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = Logger.getLogger(AdminController.class);

    // ==================== Dashboard ====================

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public ApiResponse getDashboardStats() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);

            List<SysUser> users = sysUserMapper.selectAll();
            List<UserSoft> softs = userSoftMapper.selectAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", users.size());
            stats.put("totalApps", softs.size());

            // 今日任务数（从Redis获取）
            try {
                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                String todayKey = "tasks-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int todayTasks = redisUtil.exists(todayKey) ? Integer.parseInt(redisUtil.get(todayKey)) : 0;
                stats.put("todayTasks", todayTasks);
            } catch (Exception e) {
                stats.put("todayTasks", 0);
            }

            stats.put("todayIncome", 0); // TODO: 实际项目中需要计算今日收入

            return ApiResponse.success(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘统计异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取用户增长趋势（近7天）
     */
    @GetMapping("/dashboard/user-growth")
    public ApiResponse getUserGrowthTrend() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            List<SysUser> users = sysUserMapper.selectAll();

            List<Map<String, Object>> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

                long count = users.stream()
                        .filter(u -> u.getRegTime() != null)
                        .filter(u -> {
                            LocalDateTime regTime = LocalDateTime.ofInstant(
                                    u.getRegTime().toInstant(), java.time.ZoneId.systemDefault());
                            return !regTime.isBefore(dayStart) && !regTime.isAfter(dayEnd);
                        })
                        .count();

                Map<String, Object> item = new HashMap<>();
                item.put("date", date.format(formatter));
                item.put("count", count);
                result.add(item);
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取用户增长趋势异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取应用使用统计
     */
    @GetMapping("/dashboard/app-usage")
    public ApiResponse getAppUsageStats() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            List<UserSoft> softs = userSoftMapper.selectAll();

            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd"));

            List<Map<String, Object>> result = new ArrayList<>();
            // 取前10个应用的统计数据
            int limit = Math.min(softs.size(), 10);
            for (int i = 0; i < limit; i++) {
                UserSoft soft = softs.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("appName", soft.getName());

                int userCount = 0;
                try {
                    String key = soft.getAppkey() + "-count-" + today;
                    if (redisUtil.exists(key)) {
                        userCount = redisUtil.scard(key).intValue();
                    }
                } catch (Exception ignored) {
                }
                item.put("userCount", userCount);
                result.add(item);
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取应用使用统计异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取最近登录用户
     */
    @GetMapping("/dashboard/recent-logins")
    public ApiResponse getRecentLogins() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            List<SysUser> users = sysUserMapper.selectAll();

            // 按登录次数排序，取前10个
            List<Map<String, Object>> result = users.stream()
                    .sorted((a, b) -> {
                        int countA = a.getLoginCount() != null ? a.getLoginCount() : 0;
                        int countB = b.getLoginCount() != null ? b.getLoginCount() : 0;
                        return Integer.compare(countB, countA);
                    })
                    .limit(10)
                    .map(u -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", u.getId());
                        item.put("username", u.getUsername());
                        item.put("email", u.getEmail() != null ? u.getEmail() : "-");
                        item.put("loginTime", u.getExpireTime() != null ?
                                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(u.getExpireTime()) : "-");
                        item.put("ip", "-"); // TODO: 实际项目中需要记录IP
                        return item;
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取最近登录用户异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    public ApiResponse getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            List<SysUser> allUsers = sysUserMapper.selectAll();

            // 过滤
            List<SysUser> filteredUsers = allUsers.stream()
                    .filter(u -> {
                        if (username != null && !username.isEmpty()) {
                            return u.getUsername() != null && u.getUsername().contains(username);
                        }
                        return true;
                    })
                    .filter(u -> {
                        if (email != null && !email.isEmpty()) {
                            return u.getEmail() != null && u.getEmail().contains(email);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // 分页
            int total = filteredUsers.size();
            int offset = (page - 1) * pageSize;
            List<SysUser> pageUsers = filteredUsers.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            // 转换为前端需要的格式
            List<Map<String, Object>> list = pageUsers.stream()
                    .map(u -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", u.getId());
                        item.put("username", u.getUsername());
                        item.put("email", u.getEmail());
                        item.put("regTime", u.getRegTime());
                        item.put("loginCount", u.getLoginCount());
                        item.put("vipExpire", u.getExpireTime());
                        item.put("isVip", u.getExpireTime() != null && u.getExpireTime().getTime() > System.currentTimeMillis());
                        item.put("isBanned", false); // TODO: 实际项目中需要添加封禁字段
                        item.put("lastLoginTime", u.getExpireTime());
                        item.put("lastLoginIp", "-");
                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取用户列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ApiResponse getUserDetail(@PathVariable int id) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            SysUser user = sysUserMapper.selectByPrimaryKey(id);

            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("regTime", user.getRegTime());
            result.put("loginCount", user.getLoginCount());
            result.put("vipExpire", user.getExpireTime());
            result.put("isVip", user.getExpireTime() != null && user.getExpireTime().getTime() > System.currentTimeMillis());
            result.put("isBanned", false);
            result.put("lastLoginTime", user.getExpireTime());
            result.put("lastLoginIp", "-");

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取用户详情异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 封禁用户
     */
    @PostMapping("/users/{id}/ban")
    public ApiResponse banUser(@PathVariable int id) {
        // TODO: 实际项目中需要添加封禁字段和逻辑
        return ApiResponse.success("封禁成功", null);
    }

    /**
     * 解封用户
     */
    @PostMapping("/users/{id}/unban")
    public ApiResponse unbanUser(@PathVariable int id) {
        // TODO: 实际项目中需要添加解封逻辑
        return ApiResponse.success("解封成功", null);
    }

    // ==================== 应用管理 ====================

    /**
     * 获取应用列表（分页）
     */
    @GetMapping("/softs")
    public ApiResponse getSoftList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String softName,
            @RequestParam(required = false) String packageName,
            @RequestParam(required = false) String username) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
            List<UserSoft> allSofts = userSoftMapper.selectAll();

            // 获取所有用户用于关联查询
            Map<Integer, SysUser> userMap = sysUserMapper.selectAll().stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));

            // 过滤
            List<UserSoft> filteredSofts = allSofts.stream()
                    .filter(s -> {
                        if (softName != null && !softName.isEmpty()) {
                            return s.getName() != null && s.getName().contains(softName);
                        }
                        return true;
                    })
                    .filter(s -> {
                        if (packageName != null && !packageName.isEmpty()) {
                            return s.getPackageName() != null && s.getPackageName().contains(packageName);
                        }
                        return true;
                    })
                    .filter(s -> {
                        if (username != null && !username.isEmpty()) {
                            SysUser user = userMap.get(s.getUserId());
                            return user != null && user.getUsername() != null && user.getUsername().contains(username);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // 分页
            int total = filteredSofts.size();
            int offset = (page - 1) * pageSize;
            List<UserSoft> pageSofts = filteredSofts.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            // 获取今日活跃数据
            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd"));

            List<Map<String, Object>> list = pageSofts.stream()
                    .map(s -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", s.getId());
                        item.put("softName", s.getName());
                        item.put("appKey", s.getAppkey());
                        item.put("packageName", s.getPackageName());
                        item.put("userId", s.getUserId());

                        SysUser user = userMap.get(s.getUserId());
                        item.put("username", user != null ? user.getUsername() : "-");
                        item.put("createTime", "-"); // TODO: 需要添加创建时间字段
                        item.put("totalUser", 0);
                        item.put("status", s.getHandle() != null ? s.getHandle() : 1);

                        int todayUser = 0;
                        try {
                            String key = s.getAppkey() + "-count-" + today;
                            if (redisUtil.exists(key)) {
                                todayUser = redisUtil.scard(key).intValue();
                            }
                        } catch (Exception ignored) {
                        }
                        item.put("todayUser", todayUser);

                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取应用列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取应用详情
     */
    @GetMapping("/softs/{id}")
    public ApiResponse getSoftDetail(@PathVariable int id) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);

            UserSoft soft = userSoftMapper.selectByPrimaryKey(id);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
            }

            SysUser user = sysUserMapper.selectByPrimaryKey(soft.getUserId());

            Map<String, Object> result = new HashMap<>();
            result.put("id", soft.getId());
            result.put("softName", soft.getName());
            result.put("appKey", soft.getAppkey());
            result.put("packageName", soft.getPackageName());
            result.put("userId", soft.getUserId());
            result.put("username", user != null ? user.getUsername() : "-");
            result.put("createTime", "-");
            result.put("totalUser", 0);
            result.put("todayUser", 0);
            result.put("status", soft.getHandle() != null ? soft.getHandle() : 1);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取应用详情异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 删除应用（管理员强制删除）
     */
    @DeleteMapping("/softs/{id}")
    public ApiResponse deleteSoft(@PathVariable int id) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
            SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
            SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
            SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
            SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
            SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);

            UserSoft soft = userSoftMapper.selectByPrimaryKey(id);
            if (soft == null) {
                return ApiResponse.error(404, "应用不存在");
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

    // ==================== 卡密管理 ====================

    /**
     * 获取卡密列表（分页）
     */
    @GetMapping("/cards")
    public ApiResponse getCardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String card,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Boolean usable,
            @RequestParam(required = false) Integer softId) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
            UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);

            List<SingleCard> allCards = singleCardMapper.selectAll();

            // 获取应用和用户信息用于关联
            Map<Integer, UserSoft> softMap = userSoftMapper.selectAll().stream()
                    .collect(Collectors.toMap(UserSoft::getId, s -> s));
            Map<Integer, SysUser> userMap = sysUserMapper.selectAll().stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));

            // 过滤
            List<SingleCard> filteredCards = allCards.stream()
                    .filter(c -> {
                        if (card != null && !card.isEmpty()) {
                            return c.getCard() != null && c.getCard().contains(card);
                        }
                        return true;
                    })
                    .filter(c -> type == null || (c.getType() != null && c.getType().equals(type)))
                    .filter(c -> usable == null || (c.getUsable() != null && c.getUsable().equals(usable)))
                    .filter(c -> softId == null || (c.getSoftId() != null && c.getSoftId().equals(softId)))
                    .collect(Collectors.toList());

            // 分页
            int total = filteredCards.size();
            int offset = (page - 1) * pageSize;
            List<SingleCard> pageCards = filteredCards.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<Map<String, Object>> list = pageCards.stream()
                    .map(c -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", c.getId());
                        item.put("card", c.getCard());
                        item.put("type", c.getType());
                        item.put("value", c.getValue());
                        item.put("mark", c.getMark());
                        item.put("softId", c.getSoftId());

                        UserSoft soft = softMap.get(c.getSoftId());
                        item.put("softName", soft != null ? soft.getName() : "-");

                        item.put("usable", c.getUsable());
                        item.put("userId", c.getToken()); // 使用token字段存储使用者信息
                        item.put("username", "-");
                        item.put("useTime", c.getUsrTime());
                        item.put("createTime", "-");

                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取卡密列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 获取卡密统计
     */
    @GetMapping("/cards/stats")
    public ApiResponse getCardStats() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
            List<SingleCard> allCards = singleCardMapper.selectAll();

            long used = allCards.stream().filter(c -> c.getUsable() != null && !c.getUsable()).count();
            long unused = allCards.stream().filter(c -> c.getUsable() != null && c.getUsable()).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allCards.size());
            stats.put("used", used);
            stats.put("unused", unused);
            stats.put("todayGenerated", 0); // TODO: 需要添加创建时间字段
            stats.put("todayUsed", 0);

            return ApiResponse.success(stats);
        } catch (Exception e) {
            logger.error("获取卡密统计异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 批量生成卡密
     */
    @PostMapping("/cards/generate")
    public ApiResponse generateCards(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);

            int count = params.get("count") instanceof Number ? ((Number) params.get("count")).intValue() : 0;
            int type = params.get("type") instanceof Number ? ((Number) params.get("type")).intValue() : 0;
            int value = params.get("value") instanceof Number ? ((Number) params.get("value")).intValue() : 0;
            String mark = params.getOrDefault("mark", "").toString();
            int softId = params.get("softId") instanceof Number ? ((Number) params.get("softId")).intValue() : 0;

            if (count <= 0 || count > 99) {
                return ApiResponse.error("生成数量必须在1-99之间");
            }

            List<SingleCard> cards = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                SingleCard card = new SingleCard();
                card.setCard(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
                card.setType(type);
                card.setValue(value);
                card.setMark(mark);
                card.setSoftId(softId);
                card.setUsable(true);
                cards.add(card);
            }

            singleCardMapper.insertAll(cards);
            return ApiResponse.success("生成成功", cards);
        } catch (Exception e) {
            logger.error("生成卡密异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 任务管理 ====================

    /**
     * 获取任务列表（分页）
     */
    @GetMapping("/tasks")
    public ApiResponse getTaskList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String uuid) {
        try {
            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            List<Map<String, Object>> allTasks = new ArrayList<>();

            // 从Redis获取所有任务
            Set<String> keys = redisUtil.keys("*");
            if (keys != null) {
                for (String key : keys) {
                    try {
                        String value = redisUtil.get(key);
                        if (value != null && value.startsWith("{")) {
                            Map<String, Object> task = com.alibaba.fastjson.JSONObject.parseObject(value, Map.class);
                            task.put("uuid", key);
                            allTasks.add(task);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // 过滤
            List<Map<String, Object>> filteredTasks = allTasks.stream()
                    .filter(t -> {
                        if (status != null && !status.isEmpty()) {
                            return status.equals(t.getOrDefault("status", ""));
                        }
                        return true;
                    })
                    .filter(t -> {
                        if (uuid != null && !uuid.isEmpty()) {
                            return t.getOrDefault("uuid", "").toString().contains(uuid);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // 分页
            int total = filteredTasks.size();
            int offset = (page - 1) * pageSize;
            List<Map<String, Object>> pageTasks = filteredTasks.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageTasks);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取任务列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 终止任务
     */
    @PostMapping("/tasks/stop")
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

    // ==================== 公告管理 ====================

    /**
     * 获取公告列表（分页）
     */
    @GetMapping("/notices")
    public ApiResponse getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);
            List<SysNotice> allNotices = sysNoticeMapper.selectAll();

            // 分页
            int total = allNotices.size();
            int offset = (page - 1) * pageSize;
            List<SysNotice> pageNotices = allNotices.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<Map<String, Object>> list = pageNotices.stream()
                    .map(n -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", n.getId());
                        item.put("title", n.getTitle());
                        item.put("content", n.getMsg());
                        item.put("time", n.getTime());
                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取公告列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 创建公告
     */
    @PostMapping("/notices")
    public ApiResponse createNotice(@RequestBody Map<String, String> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);

            String title = params.getOrDefault("title", "");
            String content = params.getOrDefault("content", "");

            if (title.isEmpty() || content.isEmpty()) {
                return ApiResponse.error("标题和内容不能为空");
            }

            SysNotice notice = new SysNotice();
            notice.setTitle(title);
            notice.setMsg(content);
            notice.setTime(new Date());

            if (sysNoticeMapper.insert(notice) == 1) {
                return ApiResponse.success("发布成功", null);
            } else {
                return ApiResponse.error(500, "发布失败");
            }
        } catch (Exception e) {
            logger.error("创建公告异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/notices/{id}")
    public ApiResponse updateNotice(@PathVariable int id, @RequestBody Map<String, String> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);

            SysNotice notice = sysNoticeMapper.selectByPrimaryKey(id);
            if (notice == null) {
                return ApiResponse.error(404, "公告不存在");
            }

            String title = params.getOrDefault("title", notice.getTitle());
            String content = params.getOrDefault("content", notice.getMsg());

            notice.setTitle(title);
            notice.setMsg(content);

            if (sysNoticeMapper.updateByPrimaryKey(notice) == 1) {
                return ApiResponse.success("更新成功", null);
            } else {
                return ApiResponse.error(500, "更新失败");
            }
        } catch (Exception e) {
            logger.error("更新公告异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/notices/{id}")
    public ApiResponse deleteNotice(@PathVariable int id) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);

            if (sysNoticeMapper.deleteByPrimaryKey(id) == 1) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除公告异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 版本管理 ====================

    /**
     * 获取版本列表（分页）
     */
    @GetMapping("/versions")
    public ApiResponse getVersionList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);
            List<SysVer> allVersions = sysVerMapper.selectAll();

            // 分页
            int total = allVersions.size();
            int offset = (page - 1) * pageSize;
            List<SysVer> pageVersions = allVersions.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<Map<String, Object>> list = pageVersions.stream()
                    .map(v -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", v.getId());
                        item.put("versionCode", v.getVersion());
                        item.put("versionName", v.getVersionName());
                        item.put("versionMsg", v.getVersionMsg());
                        item.put("downloadUrl", "");
                        item.put("time", v.getTime());
                        item.put("isForce", v.getVersionMode() != null && v.getVersionMode());
                        item.put("status", 1);
                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取版本列表异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 创建版本
     */
    @PostMapping("/versions")
    public ApiResponse createVersion(@RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);

            int versionCode = params.get("versionCode") instanceof Number ? ((Number) params.get("versionCode")).intValue() : 0;
            String versionName = params.getOrDefault("versionName", "").toString();
            String versionMsg = params.getOrDefault("versionMsg", "").toString();
            boolean isForce = params.get("isForce") instanceof Boolean ? (Boolean) params.get("isForce") : false;

            if (versionName.isEmpty()) {
                return ApiResponse.error("版本名称不能为空");
            }

            SysVer sysVer = new SysVer();
            sysVer.setVersion(versionCode);
            sysVer.setVersionName(versionName);
            sysVer.setVersionMsg(versionMsg);
            sysVer.setVersionMode(isForce);
            sysVer.setTime(new Date());

            if (sysVerMapper.insert(sysVer) == 1) {
                return ApiResponse.success("发布成功", null);
            } else {
                return ApiResponse.error(500, "发布失败");
            }
        } catch (Exception e) {
            logger.error("创建版本异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 更新版本
     */
    @PutMapping("/versions/{id}")
    public ApiResponse updateVersion(@PathVariable int id, @RequestBody Map<String, Object> params) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);

            SysVer sysVer = sysVerMapper.selectByPrimaryKey(id);
            if (sysVer == null) {
                return ApiResponse.error(404, "版本不存在");
            }

            if (params.containsKey("versionCode")) {
                sysVer.setVersion(((Number) params.get("versionCode")).intValue());
            }
            if (params.containsKey("versionName")) {
                sysVer.setVersionName(params.get("versionName").toString());
            }
            if (params.containsKey("versionMsg")) {
                sysVer.setVersionMsg(params.get("versionMsg").toString());
            }
            if (params.containsKey("isForce")) {
                sysVer.setVersionMode((Boolean) params.get("isForce"));
            }

            if (sysVerMapper.updateByPrimaryKey(sysVer) == 1) {
                return ApiResponse.success("更新成功", null);
            } else {
                return ApiResponse.error(500, "更新失败");
            }
        } catch (Exception e) {
            logger.error("更新版本异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    /**
     * 删除版本
     */
    @DeleteMapping("/versions/{id}")
    public ApiResponse deleteVersion(@PathVariable int id) {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);

            if (sysVerMapper.deleteByPrimaryKey(id) == 1) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除版本异常", e);
            return ApiResponse.error(500, "服务器内部错误");
        }
    }

    // ==================== 系统配置 ====================

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public ApiResponse getSystemConfig() {
        // TODO: 实际项目中需要从数据库或配置文件读取
        Map<String, Object> config = new HashMap<>();
        config.put("siteName", "ArmPro Server");
        config.put("siteUrl", "http://localhost:8080");
        config.put("adminEmail", "admin@armpro.com");
        config.put("uploadType", "local");
        config.put("qiniuAccessKey", "");
        config.put("qiniuSecretKey", "");
        config.put("qiniuBucket", "");
        config.put("qiniuDomain", "");
        config.put("localUploadPath", "/uploads");
        config.put("vipLevels", new ArrayList<>());

        return ApiResponse.success(config);
    }

    /**
     * 保存系统配置
     */
    @PostMapping("/config")
    public ApiResponse saveSystemConfig(@RequestBody Map<String, Object> params) {
        // TODO: 实际项目中需要保存到数据库或配置文件
        return ApiResponse.success("保存成功", null);
    }
}
