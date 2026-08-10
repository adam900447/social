package adam.brooks.social.service;

import adam.brooks.social.model.Report;
import adam.brooks.social.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;

    public Report reportUser(String reporterId, String reportedUserId, String reason) {
        if (reporterId.equals(reportedUserId)) {
            throw new IllegalArgumentException("You can't report yourself");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Please provide a reason for the report");
        }
        userService.getById(reportedUserId); // 404s if the target doesn't exist

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReportedUserId(reportedUserId);
        report.setReason(reason);

        return reportRepository.save(report);
    }
}
