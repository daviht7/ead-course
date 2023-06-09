package com.ead.course.controllers;

import com.ead.course.dtos.SubscriptionDto;
import com.ead.course.enums.UserStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.UserModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.UserService;
import com.ead.course.specifications.SpecificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CourseUserController {

    private final CourseService courseService;

    private final UserService userService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @GetMapping("/courses/{courseId}/users")
    public ResponseEntity<Object> getAllUserByCourse(
            SpecificationTemplate.UserSpec spec,
            @PathVariable("courseId") UUID courseId,
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {

        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);
        if (courseModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course Not Found.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll(SpecificationTemplate.userCourseId(courseId).and(spec), pageable));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @PostMapping("/courses/{courseId}/users/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(@PathVariable("courseId") UUID courseId,
                                                               @RequestBody @Valid SubscriptionDto subscriptionDto) {
        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);
        if (courseModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course Not Found.");
        }

        if (courseService.existsByCourseAndUser(courseId, subscriptionDto.getUserId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: subscription already exists!");
        }
        Optional<UserModel> userModelOptional = userService.findById(subscriptionDto.getUserId());
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        if (userModelOptional.get().getUserStatus().equals(UserStatus.BLOCKED.toString())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is blocked");
        }

        //courseService.saveSubscriptionUserInCourse(courseModelOptional.get().getCourseId(), userModelOptional.get().getUserId());
        courseService.saveSubscriptionUserInCourseAndSendNotification(courseModelOptional.get(), userModelOptional.get());

        //verificações state transfer
        return ResponseEntity.status(HttpStatus.CREATED).body("Subscription created successfully");
    }

}
