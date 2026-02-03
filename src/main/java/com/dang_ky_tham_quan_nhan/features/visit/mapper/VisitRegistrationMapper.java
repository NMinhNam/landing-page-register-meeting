package com.dang_ky_tham_quan_nhan.features.visit.mapper;

import com.dang_ky_tham_quan_nhan.features.visit.dto.AdminVisitResponse;
import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisitRegistrationMapper {

        @Insert("INSERT INTO visit_registration(soldier_id, unit_id, manual_soldier_name, manual_unit_name, representative_phone, province, visit_week, visit_week_month_display, visit_year, visit_month, status, note, created_at, approved_at) "
                        +
                        "VALUES(#{soldierId, jdbcType=BIGINT}, #{unitId, jdbcType=BIGINT}, #{manualSoldierName, jdbcType=VARCHAR}, #{manualUnitName, jdbcType=VARCHAR}, #{representativePhone}, #{province}, #{visitWeek}, #{visitWeekMonthDisplay}, #{visitYear}, #{visitMonth}, #{status}, #{note, jdbcType=VARCHAR}, #{createdAt}, #{approvedAt, jdbcType=TIMESTAMP})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        void insert(VisitRegistration registration);

        @Select("SELECT id, soldier_id, unit_id, manual_soldier_name, manual_unit_name, representative_phone, province, visit_week, visit_week_month_display, visit_year, visit_month, status, note, created_at, approved_at FROM visit_registration WHERE id = #{id}")
        VisitRegistration findById(@Param("id") Long id);

        @Update("UPDATE visit_registration SET status=#{status}, note=#{note}, approved_at=#{approvedAt}, visit_week_month_display=#{visitWeekMonthDisplay}, visit_year=#{visitYear}, visit_month=#{visitMonth} WHERE id=#{id}")
        void update(VisitRegistration registration);

        @Delete("DELETE FROM visit_registration WHERE id = #{id}")
        void deleteById(@Param("id") Long id);

        @Select("<script>" +
                        "SELECT v.id, v.soldier_id, v.unit_id, v.manual_soldier_name, v.manual_unit_name, v.representative_phone, v.province, v.visit_week, v.visit_week_month_display, v.visit_year, v.visit_month, v.status, v.note, v.created_at, v.approved_at, "
                        +
                        "COALESCE(s.name, v.manual_soldier_name) as soldier_name, " +
                        "v.representative_phone as relative_phone, " +
                        "s.unit_id, " +
                        "COALESCE(u.name, v.manual_unit_name) as unit_name, " +
                        "(SELECT GROUP_CONCAT(r.name SEPARATOR ', ') FROM relative r WHERE r.visit_registration_id = v.id) as relative_name "
                        +
                        "FROM visit_registration v " +
                        "LEFT JOIN soldier s ON v.soldier_id = s.id " +
                        "LEFT JOIN unit u ON v.unit_id = u.id " + // Use direct unit_id link
                        "WHERE (#{phone} IS NULL OR v.representative_phone = #{phone})" +
                        "</script>")
        List<Map<String, Object>> findByPhone(@Param("phone") String phone);

        @Select("<script>" +
                        "WITH RECURSIVE unit_tree AS (" +
                        "  SELECT u.id " +
                        "  FROM unit u " +
                        "  JOIN admin_user au ON au.unit_id = u.id " +
                        "  WHERE au.id = #{adminId} " +
                        "  UNION ALL " +
                        "  SELECT child.id " +
                        "  FROM unit child " +
                        "  JOIN unit_tree parent ON child.parent_id = parent.id " +
                        ") " +
                        "SELECT v.id, v.soldier_id, v.unit_id, v.manual_soldier_name, v.manual_unit_name, v.representative_phone, v.province, v.visit_week, v.visit_week_month_display, v.visit_year, v.visit_month, v.status, v.note, v.created_at, v.approved_at, "
                        +
                        "COALESCE(s.name, v.manual_soldier_name) as soldierName, " +
                        "GROUP_CONCAT(r.name SEPARATOR ', ') as relativeName, " +
                        "GROUP_CONCAT(r.id_number SEPARATOR ', ') as relativeIds, " +
                        "GROUP_CONCAT(r.relationship SEPARATOR ', ') as relationships, " +
                        "v.representative_phone as relativePhone, " +
                        "COALESCE(u.name, v.manual_unit_name) as unitName " +
                        "FROM visit_registration v " +
                        "JOIN unit_tree ut ON v.unit_id = ut.id " +
                        "LEFT JOIN soldier s ON v.soldier_id = s.id " +
                        "LEFT JOIN unit u ON v.unit_id = u.id " +
                        "LEFT JOIN relative r ON v.id = r.visit_registration_id " +
                        "WHERE 1=1 " +
                        "<if test='month != null and month != \"\"'> AND v.visit_month = CAST(#{month} AS UNSIGNED) </if>"
                        +
                        "<if test='week != null'> AND v.visit_week = #{week} </if>" +
                        "<if test='year != null and year != \"\"'> AND v.visit_year = CAST(#{year} AS UNSIGNED) </if>" +
                        "<if test='province != null and province != \"\"'> AND v.province = #{province} </if>" +
                        "<if test='status != null and status != \"\"'> AND v.status = #{status} </if>" +
                        "<if test='keyword != null and keyword != \"\"'>" +
                        " AND (" +
                        "   COALESCE(s.name, v.manual_soldier_name) LIKE CONCAT('%', #{keyword}, '%') " +
                        "   OR v.representative_phone LIKE CONCAT('%', #{keyword}, '%') " +
                        "   OR r.name LIKE CONCAT('%', #{keyword}, '%') " +
                        " )" +
                        "</if>" +
                        "GROUP BY v.id " +
                        "ORDER BY v.created_at DESC" +
                        "</script>")
        List<AdminVisitResponse> searchAdmin(
                        @Param("adminId") Long adminId,
                        @Param("month") String month,
                        @Param("week") Integer week,
                        @Param("year") String year,
                        @Param("province") String province,
                        @Param("status") String status,
                        @Param("keyword") String keyword);

        @Select("<script>" +
                        "WITH RECURSIVE unit_tree AS (" +
                        "  SELECT u.id " +
                        "  FROM unit u " +
                        "  JOIN admin_user au ON au.unit_id = u.id " +
                        "  WHERE au.id = #{adminId} " +
                        "  UNION ALL " +
                        "  SELECT child.id " +
                        "  FROM unit child " +
                        "  JOIN unit_tree parent ON child.parent_id = parent.id " +
                        ") " +
                        "SELECT v.province, COUNT(*) as count " +
                        "FROM visit_registration v " +
                        "WHERE v.unit_id IN (SELECT id FROM unit_tree) " +
                        "<if test='month != null and month != \"\"'> AND v.visit_month = CAST(#{month} AS UNSIGNED) </if>"
                        +
                        "<if test='week != null'> AND v.visit_week = #{week} </if>" +
                        "GROUP BY v.province" +
                        "</script>")
        List<Map<String, Object>> countByProvince(@Param("adminId") Long adminId,
                        @Param("month") String month,
                        @Param("week") Integer week);

        @Select("<script>" +
                        "WITH RECURSIVE unit_tree AS (" +
                        "  SELECT u.id " +
                        "  FROM unit u " +
                        "  JOIN admin_user au ON au.unit_id = u.id " +
                        "  WHERE au.id = #{adminId} " +
                        "  UNION ALL " +
                        "  SELECT child.id " +
                        "  FROM unit child " +
                        "  JOIN unit_tree parent ON child.parent_id = parent.id " +
                        ") " +
                        "SELECT status, COUNT(*) as count " +
                        "FROM visit_registration v " +
                        "WHERE v.unit_id IN (SELECT id FROM unit_tree) " +
                        "<if test='month != null and month != \"\"'> AND v.visit_month = CAST(#{month} AS UNSIGNED) </if>"
                        +
                        "<if test='week != null'> AND v.visit_week = #{week} </if>" +
                        "GROUP BY status" +
                        "</script>")
        List<Map<String, Object>> countByStatus(@Param("adminId") Long adminId,
                        @Param("month") String month,
                        @Param("week") Integer week);

        @Select("SELECT id, soldier_id, unit_id, manual_soldier_name, manual_unit_name, representative_phone, province, visit_week, visit_week_month_display, visit_year, visit_month, status, note, created_at, approved_at FROM visit_registration")
        List<VisitRegistration> findAll();
}
