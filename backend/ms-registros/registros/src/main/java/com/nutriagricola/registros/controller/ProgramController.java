package com.nutriagricola.registros.controller;
import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.*;
import com.nutriagricola.registros.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {
    private final ProgramService programs;
    public ProgramController(ProgramService programs) { this.programs = programs; }

    @GetMapping
    public ProgramPageResponse list(@RequestParam(required = false) String cropId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return programs.findAll(cropId, page, size);
    }
    @GetMapping("/{id}")
    public ProgramResponse get(@PathVariable String id) {
        return programs.findById(id);
    }
    @PostMapping
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody ProgramRequest request,
                                                  @AuthenticationPrincipal UserClientResponse user) {
        var result = programs.create(request, user);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.response());
    }
    @PutMapping("/{id}")
    public ProgramResponse update(@PathVariable String id, @Valid @RequestBody ProgramRequest request,
                                   @AuthenticationPrincipal UserClientResponse user) {
        return programs.update(id, request, user);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String id, @AuthenticationPrincipal UserClientResponse user) {
        programs.deactivate(id, user);
    }
}
