package com.example.noteice.dtos.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Note {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @JsonProperty("id")
        Long id;
        @JsonProperty("title")
        String title;
        @JsonProperty("content")
        String content;
        @JsonIgnore
        String owner;
        @JsonProperty("createdAt")
        Instant createdAt;
        @JsonProperty("lastModifiedAt")
        Instant lastModifiedAt;
}
