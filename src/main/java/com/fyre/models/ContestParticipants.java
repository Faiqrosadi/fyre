package com.fyre.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
// Tabel Participant atau user yang join suatu lomba tertentu
@Getter
@Setter
@ToString
@Entity
@Table(name = "contest_participants")
public class ContestParticipants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_participants_id")
    private long id;
    // punya id contest yang diikuti
    @Column(name = "contest_id")
    private long contestId;
    // setiap id user memiliki username
    @Column(name = "participant_username")
    private String participantUsername;
}
