package com.xinchuang.diagnosis.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinchuang.diagnosis.common.enums.ResourceStatus;
import com.xinchuang.diagnosis.dashboard.service.DashboardService;
import com.xinchuang.diagnosis.dashboard.vo.AlertItemVO;
import com.xinchuang.diagnosis.dashboard.vo.DashboardOverviewVO;
import com.xinchuang.diagnosis.dashboard.vo.StatusDistributionVO;
import com.xinchuang.diagnosis.dashboard.vo.TrendPointVO;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BusinessSystemMapper businessSystemMapper;
    private final DbResourceMapper dbResourceMapper;

    public DashboardServiceImpl(BusinessSystemMapper businessSystemMapper, DbResourceMapper dbResourceMapper) {
        this.businessSystemMapper = businessSystemMapper;
        this.dbResourceMapper = dbResourceMapper;
    }

    @Override
    public DashboardOverviewVO overview() {
        long businessTotal = businessSystemMapper.selectCount(new LambdaQueryWrapper<BusinessSystem>());
        long databaseTotal = dbResourceMapper.selectCount(new LambdaQueryWrapper<DbResource>());
        long normalTotal = countBusinessByStatus(ResourceStatus.NORMAL.name()) + countDatabaseByStatus(ResourceStatus.NORMAL.name());
        long warningTotal = countBusinessByStatus(ResourceStatus.WARNING.name()) + countDatabaseByStatus(ResourceStatus.WARNING.name());
        long dangerTotal = countBusinessByStatus(ResourceStatus.DANGER.name()) + countDatabaseByStatus(ResourceStatus.DANGER.name());
        long offlineTotal = countBusinessByStatus(ResourceStatus.OFFLINE.name()) + countDatabaseByStatus(ResourceStatus.OFFLINE.name());
        long abnormalTotal = warningTotal + dangerTotal + offlineTotal;
        long total = businessTotal + databaseTotal;

        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setBusinessSystemCount(businessTotal);
        vo.setDatabaseCount(databaseTotal);
        vo.setNormalCount(normalTotal);
        vo.setAbnormalCount(abnormalTotal);
        vo.setHealthScore(calculateHealthScore(total, warningTotal, dangerTotal, offlineTotal));
        vo.setStatusDistribution(buildStatusDistribution(normalTotal, warningTotal, dangerTotal, offlineTotal));
        vo.setTrend(buildTrend(businessTotal, databaseTotal, abnormalTotal));
        vo.setAlerts(buildAlerts());
        return vo;
    }

    private long countBusinessByStatus(String status) {
        return businessSystemMapper.selectCount(new LambdaQueryWrapper<BusinessSystem>().eq(BusinessSystem::getStatus, status));
    }

    private long countDatabaseByStatus(String status) {
        return dbResourceMapper.selectCount(new LambdaQueryWrapper<DbResource>().eq(DbResource::getStatus, status));
    }

    private int calculateHealthScore(long total, long warningTotal, long dangerTotal, long offlineTotal) {
        if (total == 0) {
            return 100;
        }
        long penalty = warningTotal * 8 + dangerTotal * 18 + offlineTotal * 24;
        return (int) Math.max(0, Math.min(100, 100 - penalty / total));
    }

    private List<StatusDistributionVO> buildStatusDistribution(long normalTotal, long warningTotal, long dangerTotal, long offlineTotal) {
        List<StatusDistributionVO> list = new ArrayList<>();
        list.add(new StatusDistributionVO(ResourceStatus.NORMAL.name(), "正常", normalTotal));
        list.add(new StatusDistributionVO(ResourceStatus.WARNING.name(), "警告", warningTotal));
        list.add(new StatusDistributionVO(ResourceStatus.DANGER.name(), "危险", dangerTotal));
        list.add(new StatusDistributionVO(ResourceStatus.OFFLINE.name(), "离线", offlineTotal));
        return list;
    }

    private List<TrendPointVO> buildTrend(long businessTotal, long databaseTotal, long abnormalTotal) {
        List<TrendPointVO> points = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        for (int i = 23; i >= 0; i--) {
            LocalDateTime time = now.minusHours(i);
            long wave = (23L - i) % 6;
            long trendAbnormal = Math.max(0, abnormalTotal + wave - 2);
            points.add(new TrendPointVO(time.format(HOUR_FORMATTER), businessTotal, databaseTotal, trendAbnormal));
        }
        return points;
    }

    private List<AlertItemVO> buildAlerts() {
        List<AlertItemVO> alerts = new ArrayList<>();
        businessSystemMapper.selectList(new LambdaQueryWrapper<BusinessSystem>()
            .ne(BusinessSystem::getStatus, ResourceStatus.NORMAL.name())
            .orderByDesc(BusinessSystem::getUpdatedAt)
            .last("LIMIT 10")
        ).forEach(item -> alerts.add(new AlertItemVO(
            "BUSINESS_SYSTEM",
            item.getId(),
            item.getName(),
            item.getStatus(),
            buildAlertMessage("业务系统", item.getName(), item.getStatus()),
            item.getUpdatedAt()
        )));
        dbResourceMapper.selectList(new LambdaQueryWrapper<DbResource>()
            .ne(DbResource::getStatus, ResourceStatus.NORMAL.name())
            .orderByDesc(DbResource::getUpdatedAt)
            .last("LIMIT 10")
        ).forEach(item -> alerts.add(new AlertItemVO(
            "DB_RESOURCE",
            item.getId(),
            item.getName(),
            item.getStatus(),
            buildAlertMessage("分布式数据库", item.getName(), item.getStatus()),
            item.getUpdatedAt()
        )));
        return alerts.stream()
            .sorted((left, right) -> {
                if (left.getUpdatedAt() == null && right.getUpdatedAt() == null) {
                    return 0;
                }
                if (left.getUpdatedAt() == null) {
                    return 1;
                }
                if (right.getUpdatedAt() == null) {
                    return -1;
                }
                return right.getUpdatedAt().compareTo(left.getUpdatedAt());
            })
            .limit(10)
            .toList();
    }

    private String buildAlertMessage(String type, String name, String status) {
        if (ResourceStatus.WARNING.name().equals(status)) {
            return type + "「" + name + "」处于警告状态，请关注运行指标。";
        }
        if (ResourceStatus.DANGER.name().equals(status)) {
            return type + "「" + name + "」处于危险状态，建议立即排查。";
        }
        if (ResourceStatus.OFFLINE.name().equals(status)) {
            return type + "「" + name + "」已离线，请检查连接状态。";
        }
        return type + "「" + name + "」状态异常，请及时确认。";
    }
}
