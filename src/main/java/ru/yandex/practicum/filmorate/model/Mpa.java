package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {
    @NotNull
    private Integer id;
    @NotBlank
    private String name;

    public Mpa(Integer id) {
        this.id = id;
    }
}
