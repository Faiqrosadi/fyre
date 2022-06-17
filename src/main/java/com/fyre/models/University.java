package com.fyre.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "university")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "univ_id")
    private int id;

//    @Column(name = "country_code")
//    private String code;

    @Column(name = "nama_universitas")
    private String name;

}
