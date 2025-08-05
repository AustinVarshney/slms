package com.java.slms.controller;

import com.java.slms.dto.ClassEntityDto;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.ClassEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class ClassEntityController {

    private final ClassEntityService classEntityService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassEntityDto>> addClass(@RequestBody ClassEntityDto classEntityDto) {
        ClassEntityDto createdClass = classEntityService.addClass(classEntityDto);
        ApiResponse<ClassEntityDto> response = ApiResponse.<ClassEntityDto>builder()
                .data(createdClass)
                .message("Class created successfully")
                .status(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassEntityDto>>> getAllClasses() {
        List<ClassEntityDto> classes = classEntityService.getAllClass();
        ApiResponse<List<ClassEntityDto>> response = ApiResponse.<List<ClassEntityDto>>builder()
                .data(classes)
                .message("Total Classes - " + classes.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/with-sections")
//    public ResponseEntity<ApiResponse<List<ClassEntityDto>>> getClassesWithSections() {
//        List<ClassEntityDto> classList = classEntityService.getClassWithSections();
//        ApiResponse<List<ClassEntityDto>> response = ApiResponse.<List<ClassEntityDto>>builder()
//                .data(classList)
//                .message("Classes with all sections retrieved successfully")
//                .status(HttpStatus.OK.value())
//                .build();
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<ClassEntityDto>> getClassByName(@PathVariable String name) {
        ClassEntityDto classDto = classEntityService.getClassByName(name);
        ApiResponse<ClassEntityDto> response = ApiResponse.<ClassEntityDto>builder()
                .data(classDto)
                .message("Class fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<ApiResponse<ClassEntityDto>> updateClassName(
            @PathVariable String name,
            @RequestBody ClassEntityDto classEntityDto
    ) {
        ClassEntityDto updatedClass = classEntityService.updateClassName(name, classEntityDto);
        ApiResponse<ClassEntityDto> response = ApiResponse.<ClassEntityDto>builder()
                .data(updatedClass)
                .message("Class name updated successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse<String>> deleteClass(@PathVariable String name) {
        classEntityService.deleteClass(name);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data(name)
                .message("Class deleted successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }
}
