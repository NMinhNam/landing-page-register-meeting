package com.dang_ky_tham_quan_nhan.features.visit.mapper;

import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisitRegistrationMapper {

    @Insert("INSERT INTO visit_registration(soldier_id, unit_id, manual_soldier_name, manual_unit_name, representative_phone, province, visit_week, status, note, created_at, approved_at) " +
            "VALUES(#{soldierId, jdbcType=BIGINT}, #{unitId, jdbcType=BIGINT}, #{manualSoldierName, jdbcType=VARCHAR}, #{manualUnitName, jdbcType=VARCHAR}, #{representativePhone}, #{province}, #{visitWeek}, #{status}, #{note, jdbcType=VARCHAR}, #{createdAt}, #{approvedAt, jdbcType=TIMESTAMP})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(VisitRegistration registration);

    @Select("SELECT * FROM visit_registration WHERE id = #{id}")
    VisitRegistration findById(@Param("id") Long id);

    @Update("UPDATE visit_registration SET status=#{status}, note=#{note}, approved_at=#{approvedAt} WHERE id=#{id}")
    void update(VisitRegistration registration);

    @Delete("DELETE FROM visit_registration WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT v.*, " +
            "COALESCE(s.name, v.manual_soldier_name) as soldier_name, " +
            "v.representative_phone as relative_phone, " +
            "s.unit_id, " +
            "COALESCE(u.name, v.manual_unit_name) as unit_name, " +
            "(SELECT GROUP_CONCAT(r.name SEPARATOR ', ') FROM relative r WHERE r.visit_registration_id = v.id) as relative_name " +
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
            "SELECT v.*, v.unit_id, " +
            "COALESCE(s.name, v.manual_soldier_name) as soldier_name, " +
            "GROUP_CONCAT(r.name SEPARATOR ', ') as relative_name, " +
            "GROUP_CONCAT(r.id_number SEPARATOR ', ') as relative_ids, " +
            "v.representative_phone as relative_phone, v.province, " +
            "COALESCE(u.name, v.manual_unit_name) as unit_name " +
            "FROM visit_registration v " +
            "LEFT JOIN soldier s ON v.soldier_id = s.id " +
            "LEFT JOIN unit u ON v.unit_id = u.id " +
            "LEFT JOIN relative r ON v.id = r.visit_registration_id " +
            "WHERE v.unit_id IN (SELECT id FROM unit_tree) " +
            "<if test='month != null and month != \"\"'> AND MONTH(v.created_at) = CAST(#{month} AS UNSIGNED) </if>" +
            "<if test='week != null'> AND v.visit_week = #{week} </if>" +
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
    List<Map<String, Object>> searchAdmin(@Param("adminId") Long adminId,
                                          @Param("month") String month,
                                          @Param("week") Integer week, 
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
            "<if test='month != null and month != \"\"'> AND MONTH(v.created_at) = CAST(#{month} AS UNSIGNED) </if>" +
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
            "<if test='month != null and month != \"\"'> AND MONTH(v.created_at) = CAST(#{month} AS UNSIGNED) </if>" +
            "<if test='week != null'> AND v.visit_week = #{week} </if>" +
            "GROUP BY status" +
            "</script>")
    List<Map<String, Object>> countByStatus(@Param("adminId") Long adminId,
                                            @Param("month") String month,
                                            @Param("week") Integer week);
}
