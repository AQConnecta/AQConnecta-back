package com.aqConnecta.DTOs.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembrosResponse {
    private MembroResponse dono;

    @Builder.Default
    private List<MembroResponse> membros = new ArrayList<>();
}
