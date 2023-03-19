package com.ead.course.services.impl;

import com.ead.course.services.UtilsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UtilsServiceImpl implements UtilsService {

    public String createUrlGetAllUsersByCourse(UUID courseId, Pageable pageable) {
        String url = "/users?courseId=" + courseId + "&page" + pageable.getPageNumber() + "&size="
                + pageable.getPageSize() + "&sort=" + pageable.getSort().toString().replaceAll(": ", ",");

        //String url2 = String.format("/courses?userId=%d&page=%d&size=%d&sort=%s", userId,pageable.getPageNumber(),
        //        pageable.getPageSize(),pageable.getSort().toString().replaceAll(": ", ","));

        return url;
    }

}
