package com.ead.course.controllers;

import com.ead.course.dtos.ModuleDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.ModuleService;
import com.ead.course.specifications.SpecificationTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("courses/{courseId}/modules")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private CourseService courseService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Object> saveModule(@PathVariable("courseId") UUID courseId,
                                             @RequestBody @Valid ModuleDto moduleDto) {

        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);

        if (courseModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course Not Found.");
        }

        var moduleModel = new ModuleModel();
        BeanUtils.copyProperties(moduleDto, moduleModel);
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        moduleModel.setCourse(courseModelOptional.get());

        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.save(moduleModel));
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("{moduleId}")
    public ResponseEntity<Object> deleteModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {

        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);

        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        moduleService.delete(moduleModelOptional.get());
        return ResponseEntity.ok("module deleted successfully");
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("{moduleId}")
    public ResponseEntity<Object> updateModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId,
                                               @RequestBody @Valid ModuleDto moduleDto) {

        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);

        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        var moduleModel = moduleModelOptional.get();
        moduleModel.setTitle(moduleDto.getTitle());
        moduleModel.setDescription(moduleDto.getDescription());

        return ResponseEntity.status(HttpStatus.OK).body(moduleService.save(moduleModel));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping
    public ResponseEntity<Page<ModuleModel>> getAllModules(@PathVariable("courseId") UUID courseId,
                                                           SpecificationTemplate.ModuleSpec spec,
                                                           @PageableDefault(page = 0, size = 10, sort = "moduleId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(moduleService.findAllModuleByCourse(SpecificationTemplate.moduleCourseId(courseId).and(spec), pageable));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("{moduleId}")
    public ResponseEntity<Object> getOneModule(@PathVariable("courseId") UUID courseId,
                                               @PathVariable("moduleId") UUID moduleId) {

        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);

        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }

        return ResponseEntity.ok(moduleModelOptional.get());
    }

}
