package com.xinchuang.diagnosis.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinchuang.diagnosis.system.entity.SysMenu;
import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysRoleMenu;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.entity.SystemNotification;
import com.xinchuang.diagnosis.system.entity.SysConfig;
import com.xinchuang.diagnosis.system.entity.SysDictItem;
import com.xinchuang.diagnosis.system.entity.SysDictType;
import com.xinchuang.diagnosis.system.mapper.SysConfigMapper;
import com.xinchuang.diagnosis.system.mapper.SysDictItemMapper;
import com.xinchuang.diagnosis.system.mapper.SysDictTypeMapper;
import com.xinchuang.diagnosis.system.mapper.SysMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SystemNotificationMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class SystemBootstrapInitializer implements ApplicationRunner {
    private static final String ADMIN_ROLE_CODE = "admin";
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictItemMapper sysDictItemMapper;
    private final SysConfigMapper sysConfigMapper;
    private final SystemNotificationMapper systemNotificationMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final boolean bootstrapAdminEnabled;
    private final String bootstrapAdminUsername;
    private final String bootstrapAdminPassword;

    public SystemBootstrapInitializer(SysUserMapper sysUserMapper,
                                      SysRoleMapper sysRoleMapper,
                                      SysUserRoleMapper sysUserRoleMapper,
                                      SysMenuMapper sysMenuMapper,
                                      SysRoleMenuMapper sysRoleMenuMapper,
                                      SysDictTypeMapper sysDictTypeMapper,
                                      SysDictItemMapper sysDictItemMapper,
                                      SysConfigMapper sysConfigMapper,
                                      SystemNotificationMapper systemNotificationMapper,
                                      BCryptPasswordEncoder passwordEncoder,
                                      @Value("${security.bootstrap-admin.enabled:false}") boolean bootstrapAdminEnabled,
                                      @Value("${security.bootstrap-admin.username:}") String bootstrapAdminUsername,
                                      @Value("${security.bootstrap-admin.password:}") String bootstrapAdminPassword) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictItemMapper = sysDictItemMapper;
        this.sysConfigMapper = sysConfigMapper;
        this.systemNotificationMapper = systemNotificationMapper;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminEnabled = bootstrapAdminEnabled;
        this.bootstrapAdminUsername = StringUtils.hasText(bootstrapAdminUsername) ? bootstrapAdminUsername : ADMIN_ROLE_CODE;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        ensureRoles();
        ensureDictionaries();
        ensureConfigs();
        ensureMenus();
        ensureRoleMenus();
        SysUser admin = ensureBootstrapAdmin(getRole(ADMIN_ROLE_CODE));
        if (admin != null) {
            ensureDemoNotifications(admin);
        }
    }

    private SysUser ensureBootstrapAdmin(SysRole adminRole) {
        SysUser admin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, bootstrapAdminUsername));
        if (admin == null && bootstrapAdminEnabled) {
            if (!StringUtils.hasText(bootstrapAdminPassword)) {
                throw new IllegalStateException("启用管理员初始化时必须配置 BOOTSTRAP_ADMIN_PASSWORD");
            }
            admin = new SysUser();
            admin.setUsername(bootstrapAdminUsername);
            admin.setPasswordHash(passwordEncoder.encode(bootstrapAdminPassword));
            admin.setRealName("系统管理员");
            admin.setDepartment("平台管理部");
            admin.setPhone("13800000000");
            admin.setEmail("admin@example.com");
            admin.setStatus("ENABLED");
            admin.setRemark("由环境配置初始化的管理员账号");
            sysUserMapper.insert(admin);
        }
        if (admin != null && adminRole != null) {
            ensureUserRole(admin.getId(), adminRole.getId());
        }
        return admin;
    }

    private void ensureRoles() {
        List<RoleSeed> roles = List.of(
            new RoleSeed(ADMIN_ROLE_CODE, "系统管理员", "拥有系统管理权限，菜单和按钮权限后续扩展"),
            new RoleSeed("operator", "运维操作员", "负责台账维护、告警处理和诊断任务操作"),
            new RoleSeed("viewer", "只读查看员", "只读查看数据大屏、台账和报表")
        );
        for (RoleSeed seed : roles) {
            SysRole role = getRole(seed.roleCode());
            if (role == null) {
                role = new SysRole();
                role.setRoleCode(seed.roleCode());
                role.setRoleName(seed.roleName());
                role.setStatus("ENABLED");
                role.setRemark(seed.remark());
                sysRoleMapper.insert(role);
            }
        }
    }

    private void ensureMenus() {
        List<MenuSeed> seeds = List.of(
            new MenuSeed(1900000000000002001L, 0L, "MENU", "数据台账", "ledger:view", null, "Coin", 10, 1, "数据台账一级菜单"),
            new MenuSeed(1900000000000002002L, 1900000000000002001L, "MENU", "数据可视化台账", "ledger:visualization:view", "/data-ledger/visualization", null, 11, 1, "数据台账页面"),
            new MenuSeed(1900000000000002003L, 1900000000000002002L, "BUTTON", "业务系统新增", "ledger:business:create", null, null, 12, 0, "业务系统新增按钮"),
            new MenuSeed(1900000000000002004L, 1900000000000002002L, "BUTTON", "业务系统编辑", "ledger:business:update", null, null, 13, 0, "业务系统编辑按钮"),
            new MenuSeed(1900000000000002005L, 1900000000000002002L, "BUTTON", "业务系统删除", "ledger:business:delete", null, null, 14, 0, "业务系统删除按钮"),
            new MenuSeed(1900000000000002006L, 1900000000000002002L, "BUTTON", "数据库新增", "ledger:database:create", null, null, 15, 0, "分布式数据库新增按钮"),
            new MenuSeed(1900000000000002007L, 1900000000000002002L, "BUTTON", "数据库编辑", "ledger:database:update", null, null, 16, 0, "分布式数据库编辑按钮"),
            new MenuSeed(1900000000000002008L, 1900000000000002002L, "BUTTON", "数据库删除", "ledger:database:delete", null, null, 17, 0, "分布式数据库删除按钮"),
            new MenuSeed(1900000000000002010L, 0L, "MENU", "数据大屏", "dashboard:view", "/dashboard", "Monitor", 20, 1, "数据大屏"),
            new MenuSeed(1900000000000002020L, 0L, "MENU", "AI故障预测", "fault-prediction:view", null, "TrendCharts", 30, 1, "AI故障预测一级菜单"),
            new MenuSeed(1900000000000002021L, 1900000000000002020L, "MENU", "故障知识库管理", "fault-prediction:knowledge:view", "/fault-prediction/knowledge", null, 31, 1, "故障知识库"),
            new MenuSeed(1900000000000002022L, 1900000000000002020L, "MENU", "故障预测任务配置", "fault-prediction:task:view", "/fault-prediction/task", null, 32, 1, "故障预测任务"),
            new MenuSeed(1900000000000002023L, 1900000000000002020L, "MENU", "故障智能预警", "fault-prediction:alert:view", "/fault-prediction/alert", null, 33, 1, "故障智能预警"),
            new MenuSeed(1900000000000002110L, 1900000000000002020L, "BUTTON", "AI模型查看", "ai:model:view", null, null, 34, 0, "AI模型查看"),
            new MenuSeed(1900000000000002111L, 1900000000000002020L, "BUTTON", "AI模型新增", "ai:model:create", null, null, 35, 0, "AI模型新增"),
            new MenuSeed(1900000000000002112L, 1900000000000002020L, "BUTTON", "AI模型编辑", "ai:model:update", null, null, 36, 0, "AI模型编辑"),
            new MenuSeed(1900000000000002113L, 1900000000000002020L, "BUTTON", "AI模型删除", "ai:model:delete", null, null, 37, 0, "AI模型删除"),
            new MenuSeed(1900000000000002114L, 1900000000000002021L, "BUTTON", "知识库新增", "ai:knowledge:create", null, null, 38, 0, "知识库新增"),
            new MenuSeed(1900000000000002115L, 1900000000000002021L, "BUTTON", "知识库编辑", "ai:knowledge:update", null, null, 39, 0, "知识库编辑"),
            new MenuSeed(1900000000000002116L, 1900000000000002021L, "BUTTON", "知识库删除", "ai:knowledge:delete", null, null, 40, 0, "知识库删除"),
            new MenuSeed(1900000000000002117L, 1900000000000002021L, "BUTTON", "知识库查看", "ai:knowledge:view", null, null, 41, 0, "知识库查看"),
            new MenuSeed(1900000000000002118L, 1900000000000002022L, "BUTTON", "AI任务新增", "ai:task:create", null, null, 42, 0, "AI任务新增"),
            new MenuSeed(1900000000000002119L, 1900000000000002022L, "BUTTON", "AI任务编辑", "ai:task:update", null, null, 43, 0, "AI任务编辑"),
            new MenuSeed(1900000000000002120L, 1900000000000002022L, "BUTTON", "AI任务删除", "ai:task:delete", null, null, 44, 0, "AI任务删除"),
            new MenuSeed(1900000000000002121L, 1900000000000002022L, "BUTTON", "AI任务执行", "ai:task:execute", null, null, 45, 0, "AI任务执行"),
            new MenuSeed(1900000000000002122L, 1900000000000002022L, "BUTTON", "AI任务查看", "ai:task:view", null, null, 46, 0, "AI任务查看"),
            new MenuSeed(1900000000000002123L, 1900000000000002023L, "BUTTON", "故障预警新增", "ai:fault:create", null, null, 47, 0, "故障预警新增"),
            new MenuSeed(1900000000000002124L, 1900000000000002023L, "BUTTON", "故障预警编辑", "ai:fault:update", null, null, 48, 0, "故障预警编辑"),
            new MenuSeed(1900000000000002125L, 1900000000000002023L, "BUTTON", "故障预警删除", "ai:fault:delete", null, null, 49, 0, "故障预警删除"),
            new MenuSeed(1900000000000002126L, 1900000000000002023L, "BUTTON", "故障预警处理", "ai:fault:handle", null, null, 50, 0, "故障预警处理"),
            new MenuSeed(1900000000000002127L, 1900000000000002023L, "BUTTON", "故障预警查看", "ai:fault:view", null, null, 51, 0, "故障预警查看"),
            new MenuSeed(1900000000000002030L, 0L, "MENU", "AI缺陷检测", "defect-detection:view", null, "Warning", 40, 1, "AI缺陷检测一级菜单"),
            new MenuSeed(1900000000000002031L, 1900000000000002030L, "MENU", "缺陷知识库管理", "defect-detection:knowledge:view", "/defect-detection/knowledge", null, 41, 1, "缺陷知识库"),
            new MenuSeed(1900000000000002032L, 1900000000000002030L, "MENU", "缺陷检测任务配置", "defect-detection:task:view", "/defect-detection/task", null, 42, 1, "缺陷检测任务"),
            new MenuSeed(1900000000000002033L, 1900000000000002030L, "MENU", "缺陷检测报表", "defect-detection:report:view", "/defect-detection/report", null, 43, 1, "缺陷检测报表"),
            new MenuSeed(1900000000000002130L, 1900000000000002033L, "BUTTON", "缺陷报表新增", "ai:defect:create", null, null, 44, 0, "缺陷报表新增"),
            new MenuSeed(1900000000000002131L, 1900000000000002033L, "BUTTON", "缺陷报表编辑", "ai:defect:update", null, null, 45, 0, "缺陷报表编辑"),
            new MenuSeed(1900000000000002132L, 1900000000000002033L, "BUTTON", "缺陷报表删除", "ai:defect:delete", null, null, 46, 0, "缺陷报表删除"),
            new MenuSeed(1900000000000002133L, 1900000000000002033L, "BUTTON", "缺陷报表处理", "ai:defect:handle", null, null, 47, 0, "缺陷报表处理"),
            new MenuSeed(1900000000000002134L, 1900000000000002033L, "BUTTON", "缺陷报表查看", "ai:defect:view", null, null, 48, 0, "缺陷报表查看"),
            new MenuSeed(1900000000000002040L, 0L, "MENU", "AI性能优化", "performance:view", null, "Odometer", 50, 1, "AI性能优化一级菜单"),
            new MenuSeed(1900000000000002041L, 1900000000000002040L, "MENU", "优化知识库管理", "performance:knowledge:view", "/performance/knowledge", null, 51, 1, "优化知识库"),
            new MenuSeed(1900000000000002042L, 1900000000000002040L, "MENU", "性能优化任务配置", "performance:task:view", "/performance/task", null, 52, 1, "性能优化任务"),
            new MenuSeed(1900000000000002043L, 1900000000000002040L, "MENU", "性能优化建议", "performance:suggestion:view", "/performance/suggestion", null, 53, 1, "性能优化建议"),
            new MenuSeed(1900000000000002140L, 1900000000000002043L, "BUTTON", "优化建议新增", "ai:optimization:create", null, null, 54, 0, "优化建议新增"),
            new MenuSeed(1900000000000002141L, 1900000000000002043L, "BUTTON", "优化建议编辑", "ai:optimization:update", null, null, 55, 0, "优化建议编辑"),
            new MenuSeed(1900000000000002142L, 1900000000000002043L, "BUTTON", "优化建议删除", "ai:optimization:delete", null, null, 56, 0, "优化建议删除"),
            new MenuSeed(1900000000000002143L, 1900000000000002043L, "BUTTON", "优化建议处理", "ai:optimization:handle", null, null, 57, 0, "优化建议处理"),
            new MenuSeed(1900000000000002144L, 1900000000000002043L, "BUTTON", "优化建议查看", "ai:optimization:view", null, null, 58, 0, "优化建议查看"),
            new MenuSeed(1900000000000002050L, 0L, "MENU", "系统管理", "system:view", null, "Setting", 90, 1, "系统管理一级菜单"),
            new MenuSeed(1900000000000002051L, 1900000000000002050L, "MENU", "用户账号管理", "system:user:view", "/system/users", null, 91, 1, "用户账号管理"),
            new MenuSeed(1900000000000002052L, 1900000000000002050L, "MENU", "角色权限管理", "system:role:view", "/system/roles", null, 92, 1, "角色权限管理"),
            new MenuSeed(1900000000000002053L, 1900000000000002052L, "BUTTON", "菜单权限维护", "system:menu:view", null, null, 93, 0, "角色权限管理中的菜单权限维护入口"),
            new MenuSeed(1900000000000002054L, 1900000000000002050L, "MENU", "日志审计管理", "system:audit-log:view", "/system/audit", null, 93, 1, "登录日志和操作日志审计"),
            new MenuSeed(1900000000000002055L, 1900000000000002056L, "BUTTON", "字典配置查看", "system:dict:view", null, null, 95, 0, "系统基础配置中的字典管理权限"),
            new MenuSeed(1900000000000002056L, 1900000000000002050L, "MENU", "系统基础配置", "system:config:view", "/system/config", null, 94, 1, "字典和系统参数的统一配置入口"),
            new MenuSeed(1900000000000002057L, 1900000000000002050L, "MENU", "安全登录管控", "system:security:view", "/system/security", null, 95, 1, "登录安全审计入口"),
            new MenuSeed(1900000000000002061L, 1900000000000002051L, "BUTTON", "用户新增", "system:user:create", null, null, 101, 0, "用户新增按钮"),
            new MenuSeed(1900000000000002062L, 1900000000000002051L, "BUTTON", "用户编辑", "system:user:update", null, null, 95, 0, "用户编辑按钮"),
            new MenuSeed(1900000000000002063L, 1900000000000002051L, "BUTTON", "用户启用禁用", "system:user:status", null, null, 96, 0, "用户启用禁用按钮"),
            new MenuSeed(1900000000000002064L, 1900000000000002051L, "BUTTON", "用户重置密码", "system:user:reset-password", null, null, 97, 0, "用户重置密码按钮"),
            new MenuSeed(1900000000000002065L, 1900000000000002051L, "BUTTON", "用户删除", "system:user:delete", null, null, 98, 0, "用户删除按钮"),
            new MenuSeed(1900000000000002071L, 1900000000000002052L, "BUTTON", "角色新增", "system:role:create", null, null, 99, 0, "角色新增按钮"),
            new MenuSeed(1900000000000002072L, 1900000000000002052L, "BUTTON", "角色编辑", "system:role:update", null, null, 100, 0, "角色编辑按钮"),
            new MenuSeed(1900000000000002073L, 1900000000000002052L, "BUTTON", "角色启用禁用", "system:role:status", null, null, 101, 0, "角色启用禁用按钮"),
            new MenuSeed(1900000000000002074L, 1900000000000002052L, "BUTTON", "角色删除", "system:role:delete", null, null, 102, 0, "角色删除按钮"),
            new MenuSeed(1900000000000002075L, 1900000000000002052L, "BUTTON", "角色分配菜单权限", "system:role:assign-menu", null, null, 103, 0, "角色分配菜单权限按钮"),
            new MenuSeed(1900000000000002081L, 1900000000000002053L, "BUTTON", "菜单新增", "system:menu:create", null, null, 104, 0, "菜单新增按钮"),
            new MenuSeed(1900000000000002082L, 1900000000000002053L, "BUTTON", "菜单编辑", "system:menu:update", null, null, 105, 0, "菜单编辑按钮"),
            new MenuSeed(1900000000000002083L, 1900000000000002053L, "BUTTON", "菜单启用禁用", "system:menu:status", null, null, 106, 0, "菜单启用禁用按钮"),
            new MenuSeed(1900000000000002084L, 1900000000000002053L, "BUTTON", "菜单删除", "system:menu:delete", null, null, 107, 0, "菜单删除按钮"),
            new MenuSeed(1900000000000002091L, 1900000000000002055L, "BUTTON", "字典新增", "system:dict:create", null, null, 108, 0, "字典新增按钮"),
            new MenuSeed(1900000000000002092L, 1900000000000002055L, "BUTTON", "字典编辑", "system:dict:update", null, null, 109, 0, "字典编辑按钮"),
            new MenuSeed(1900000000000002093L, 1900000000000002055L, "BUTTON", "字典启用禁用", "system:dict:status", null, null, 110, 0, "字典启用禁用按钮"),
            new MenuSeed(1900000000000002094L, 1900000000000002055L, "BUTTON", "字典删除", "system:dict:delete", null, null, 111, 0, "字典删除按钮"),
            new MenuSeed(1900000000000002101L, 1900000000000002056L, "BUTTON", "配置新增", "system:config:create", null, null, 112, 0, "配置新增按钮"),
            new MenuSeed(1900000000000002102L, 1900000000000002056L, "BUTTON", "配置编辑", "system:config:update", null, null, 113, 0, "配置编辑按钮"),
            new MenuSeed(1900000000000002103L, 1900000000000002056L, "BUTTON", "配置启用禁用", "system:config:status", null, null, 114, 0, "配置启用禁用按钮"),
            new MenuSeed(1900000000000002104L, 1900000000000002056L, "BUTTON", "配置删除", "system:config:delete", null, null, 115, 0, "配置删除按钮")
        );
        for (MenuSeed seed : seeds) {
            SysMenu menu = sysMenuMapper.selectById(seed.id());
            boolean exists = menu != null;
            if (!exists) {
                menu = new SysMenu();
                menu.setId(seed.id());
            }
            menu.setParentId(seed.parentId());
            menu.setMenuType(seed.menuType());
            menu.setMenuName(seed.menuName());
            menu.setPermissionCode(seed.permissionCode());
            menu.setRoutePath(seed.routePath());
            menu.setIcon(seed.icon());
            menu.setSortOrder(seed.sortOrder());
            menu.setVisible(seed.visible());
            menu.setStatus("ENABLED");
            menu.setRemark(seed.remark());
            if (exists) {
                sysMenuMapper.updateById(menu);
            } else {
                sysMenuMapper.insert(menu);
            }
        }
    }

    private void ensureRoleMenus() {
        SysRole admin = getRole("admin");
        SysRole operator = getRole("operator");
        SysRole viewer = getRole("viewer");
        if (admin != null) {
            reconcileRoleMenus(admin.getId(), sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()).stream().map(SysMenu::getId).toList());
        }
        List<Long> viewerMenus = List.of(
            1900000000000002001L, 1900000000000002002L, 1900000000000002010L,
            1900000000000002020L, 1900000000000002021L, 1900000000000002022L, 1900000000000002023L,
            1900000000000002030L, 1900000000000002031L, 1900000000000002032L, 1900000000000002033L,
            1900000000000002040L, 1900000000000002041L, 1900000000000002042L, 1900000000000002043L,
            1900000000000002110L, 1900000000000002117L, 1900000000000002122L, 1900000000000002127L,
            1900000000000002134L, 1900000000000002144L
        );
        if (viewer != null) {
            reconcileRoleMenus(viewer.getId(), viewerMenus);
        }
        List<Long> operatorMenus = List.of(
            1900000000000002001L, 1900000000000002002L, 1900000000000002003L, 1900000000000002004L,
            1900000000000002005L, 1900000000000002006L, 1900000000000002007L, 1900000000000002008L,
            1900000000000002010L, 1900000000000002020L, 1900000000000002021L, 1900000000000002022L,
            1900000000000002023L, 1900000000000002030L, 1900000000000002031L, 1900000000000002032L,
            1900000000000002033L, 1900000000000002040L, 1900000000000002041L, 1900000000000002042L,
            1900000000000002043L,
            1900000000000002110L, 1900000000000002111L, 1900000000000002112L, 1900000000000002113L,
            1900000000000002114L, 1900000000000002115L, 1900000000000002117L,
            1900000000000002118L, 1900000000000002119L, 1900000000000002121L,
            1900000000000002122L, 1900000000000002126L, 1900000000000002127L, 1900000000000002130L, 1900000000000002131L,
            1900000000000002133L, 1900000000000002134L, 1900000000000002140L,
            1900000000000002141L, 1900000000000002143L, 1900000000000002144L
        );
        if (operator != null) {
            reconcileRoleMenus(operator.getId(), operatorMenus);
        }
    }

    /**
     * Keeps only bootstrap roles aligned with their documented default permissions.
     * Custom roles remain fully managed through the role permission screen.
     */
    private void reconcileRoleMenus(Long roleId, List<Long> menuIds) {
        Set<Long> expectedMenuIds = new HashSet<>(menuIds);
        Set<Long> currentMenuIds = new HashSet<>(sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .toList());
        if (currentMenuIds.equals(expectedMenuIds)) {
            return;
        }
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : expectedMenuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
    }

    private void ensureDictionaries() {
        List<DictTypeSeed> types = List.of(
            new DictTypeSeed("HEALTH_STATUS", "健康状态", "台账和大屏使用的资源健康状态"),
            new DictTypeSeed("ENABLE_STATUS", "启用状态", "通用启用禁用状态"),
            new DictTypeSeed("LOGIN_STATUS", "登录状态", "登录审计结果状态"),
            new DictTypeSeed("OPERATION_TYPE", "操作类型", "操作日志动作类型"),
            new DictTypeSeed("TASK_TYPE", "任务类型", "AI任务类型预留"),
            new DictTypeSeed("NOTICE_TYPE", "通知类型", "通知中心消息类型"),
            new DictTypeSeed("NOTICE_PRIORITY", "通知优先级", "通知中心消息优先级"),
            new DictTypeSeed("NOTICE_READ_STATUS", "通知阅读状态", "通知中心已读未读状态")
        );
        for (DictTypeSeed seed : types) {
            if (sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictCode, seed.dictCode())) == null) {
                SysDictType type = new SysDictType();
                type.setDictCode(seed.dictCode());
                type.setDictName(seed.dictName());
                type.setStatus("ENABLED");
                type.setRemark(seed.remark());
                sysDictTypeMapper.insert(type);
            }
        }

        List<DictItemSeed> items = List.of(
            new DictItemSeed("HEALTH_STATUS", "NORMAL", "正常", "success", 10),
            new DictItemSeed("HEALTH_STATUS", "WARNING", "警告", "warning", 20),
            new DictItemSeed("HEALTH_STATUS", "DANGER", "危险", "danger", 30),
            new DictItemSeed("HEALTH_STATUS", "OFFLINE", "离线", "info", 40),
            new DictItemSeed("ENABLE_STATUS", "ENABLED", "启用", "success", 10),
            new DictItemSeed("ENABLE_STATUS", "DISABLED", "禁用", "info", 20),
            new DictItemSeed("LOGIN_STATUS", "SUCCESS", "成功", "success", 10),
            new DictItemSeed("LOGIN_STATUS", "FAILED", "失败", "danger", 20),
            new DictItemSeed("OPERATION_TYPE", "CREATE", "新增", "success", 10),
            new DictItemSeed("OPERATION_TYPE", "UPDATE", "编辑", "primary", 20),
            new DictItemSeed("OPERATION_TYPE", "DELETE", "删除", "danger", 30),
            new DictItemSeed("OPERATION_TYPE", "STATUS", "状态变更", "warning", 40),
            new DictItemSeed("OPERATION_TYPE", "RESET_PASSWORD", "重置密码", "warning", 50),
            new DictItemSeed("OPERATION_TYPE", "ASSIGN_PERMISSION", "分配权限", "primary", 60),
            new DictItemSeed("OPERATION_TYPE", "READ", "标记已读", "primary", 70),
            new DictItemSeed("OPERATION_TYPE", "READ_ALL", "全部已读", "primary", 80),
            new DictItemSeed("OPERATION_TYPE", "CLEAR_READ", "清空已读", "warning", 90),
            new DictItemSeed("TASK_TYPE", "FAULT_PREDICTION", "故障预测", "danger", 10),
            new DictItemSeed("TASK_TYPE", "DEFECT_DETECTION", "缺陷检测", "warning", 20),
            new DictItemSeed("TASK_TYPE", "PERFORMANCE_OPTIMIZATION", "性能优化", "success", 30),
            new DictItemSeed("NOTICE_TYPE", "SYSTEM", "系统通知", "primary", 10),
            new DictItemSeed("NOTICE_TYPE", "ALERT", "告警通知", "danger", 20),
            new DictItemSeed("NOTICE_TYPE", "TASK", "任务通知", "warning", 30),
            new DictItemSeed("NOTICE_TYPE", "SECURITY", "安全通知", "info", 40),
            new DictItemSeed("NOTICE_PRIORITY", "LOW", "低", "info", 10),
            new DictItemSeed("NOTICE_PRIORITY", "NORMAL", "普通", "primary", 20),
            new DictItemSeed("NOTICE_PRIORITY", "HIGH", "高", "danger", 30),
            new DictItemSeed("NOTICE_READ_STATUS", "UNREAD", "未读", "warning", 10),
            new DictItemSeed("NOTICE_READ_STATUS", "READ", "已读", "success", 20)
        );
        for (DictItemSeed seed : items) {
            Long count = sysDictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, seed.dictCode())
                .eq(SysDictItem::getItemValue, seed.itemValue()));
            if (count != null && count > 0) {
                continue;
            }
            SysDictItem item = new SysDictItem();
            item.setDictCode(seed.dictCode());
            item.setItemValue(seed.itemValue());
            item.setItemLabel(seed.itemLabel());
            item.setTagType(seed.tagType());
            item.setSortOrder(seed.sortOrder());
            item.setStatus("ENABLED");
            sysDictItemMapper.insert(item);
        }
    }

    private void ensureConfigs() {
        List<ConfigSeed> configs = List.of(
            new ConfigSeed("SYSTEM_NAME", "系统名称", "信创数据库智能故障诊断及修复平台", "TEXT", "平台显示名称"),
            new ConfigSeed("TOKEN_EXPIRE_SECONDS", "Token有效期秒数", "7200", "NUMBER", "JWT登录凭证有效期，当前阶段仅作为配置项预留"),
            new ConfigSeed("DASHBOARD_REFRESH_SECONDS", "数据大屏刷新秒数", "30", "NUMBER", "数据大屏自动刷新间隔，当前阶段仅作为配置项预留"),
            new ConfigSeed("DEFAULT_PASSWORD", "默认密码", "", "TEXT", "重置密码必须由管理员在系统配置中设置，禁止内置弱密码")
        );
        for (ConfigSeed seed : configs) {
            SysConfig existing = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, seed.configKey()));
            if (existing == null) {
                SysConfig config = new SysConfig();
                config.setConfigKey(seed.configKey());
                config.setConfigName(seed.configName());
                config.setConfigValue(seed.configValue());
                config.setConfigType(seed.configType());
                config.setStatus("ENABLED");
                config.setRemark(seed.remark());
                sysConfigMapper.insert(config);
            } else if ("DEFAULT_PASSWORD".equals(seed.configKey()) && "password".equals(existing.getConfigValue())) {
                existing.setConfigValue("");
                existing.setRemark(seed.remark());
                sysConfigMapper.updateById(existing);
            }
        }
    }

    private void ensureDemoNotifications(SysUser admin) {
        Long count = systemNotificationMapper.selectCount(new LambdaQueryWrapper<SystemNotification>()
            .eq(SystemNotification::getReceiverId, admin.getId()));
        if (count != null && count > 0) {
            return;
        }
        List<NotificationSeed> seeds = List.of(
            new NotificationSeed("欢迎使用通知中心", "通知中心已接入真实数据库，后续台账异常、用户安全事件等消息会在这里汇总。", "SYSTEM", "NORMAL"),
            new NotificationSeed("数据台账告警提醒", "当业务系统或分布式数据库状态变为警告、危险或离线时，系统会自动生成告警通知。", "ALERT", "HIGH")
        );
        for (NotificationSeed seed : seeds) {
            SystemNotification notification = new SystemNotification();
            notification.setReceiverId(admin.getId());
            notification.setReceiverNameSnapshot(admin.getRealName());
            notification.setTitle(seed.title());
            notification.setContent(seed.content());
            notification.setNoticeType(seed.noticeType());
            notification.setPriority(seed.priority());
            notification.setReadStatus("UNREAD");
            systemNotificationMapper.insert(notification);
        }
    }

    private SysRole getRole(String roleCode) {
        return sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleCode, roleCode));
    }

    private void ensureUserRole(Long userId, Long roleId) {
        Long count = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getUserId, userId)
            .eq(SysUserRole::getRoleId, roleId));
        if (count != null && count > 0) {
            return;
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
    }

    private record RoleSeed(String roleCode, String roleName, String remark) {
    }

    private record MenuSeed(Long id, Long parentId, String menuType, String menuName, String permissionCode,
                            String routePath, String icon, Integer sortOrder, Integer visible, String remark) {
    }

    private record DictTypeSeed(String dictCode, String dictName, String remark) {
    }

    private record DictItemSeed(String dictCode, String itemValue, String itemLabel, String tagType, Integer sortOrder) {
    }

    private record ConfigSeed(String configKey, String configName, String configValue, String configType, String remark) {
    }

    private record NotificationSeed(String title, String content, String noticeType, String priority) {
    }
}
