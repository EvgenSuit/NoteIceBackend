package com.example.noteice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @JsonProperty("id")
        Long id;
        @JsonProperty("title")
        String title;
        @JsonProperty("content")
        String content;
        @JsonProperty("owner")
        String owner;
}
