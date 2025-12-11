package com.dang_ky_tham_quan_nhan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dang_ky_tham_quan_nhan.features.auth.mapper")
@MapperScan("com.dang_ky_tham_quan_nhan.features.unit.mapper")
@MapperScan("com.dang_ky_tham_quan_nhan.features.soldier.mapper")
@MapperScan("com.dang_ky_tham_quan_nhan.features.visit.mapper")
public class DangKyThamQuanNhanApplication {

    public static void main(String[] args) {
        SpringApplication.run(DangKyThamQuanNhanApplication.class, args);
    }

}
