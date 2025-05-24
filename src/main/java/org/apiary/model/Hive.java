package org.apiary.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Hive")
public class Hive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hiveId")
    private Integer hiveId;

    @Column(name = "hiveNumber", nullable = false)
    private Integer hiveNumber;

    @Column(name = "queenYear")
    private Integer queenYear;

    @ManyToOne
    @JoinColumn(name = "apiaryId", nullable = false)
    private Apiary apiary;

    @OneToMany(mappedBy = "hive", cascade = CascadeType.ALL)
    private List<HoneyProduct> honeyProducts = new ArrayList<>();

    // Default constructor required by JPA
    public Hive() {}

    public Hive(Integer hiveNumber, Integer queenYear, Apiary apiary) {
        this.hiveNumber = hiveNumber;
        this.queenYear = queenYear;
        this.apiary = apiary;
    }

    // Getters and setters
    public Integer getHiveId() {
        return hiveId;
    }

    public void setHiveId(Integer hiveId) {
        this.hiveId = hiveId;
    }

    public Integer getHiveNumber() {
        return hiveNumber;
    }

    public void setHiveNumber(Integer hiveNumber) {
        this.hiveNumber = hiveNumber;
    }

    public Integer getQueenYear() {
        return queenYear;
    }

    public void setQueenYear(Integer queenYear) {
        this.queenYear = queenYear;
    }

    public Apiary getApiary() {
        return apiary;
    }

    public void setApiary(Apiary apiary) {
        this.apiary = apiary;
    }

    public List<HoneyProduct> getHoneyProducts() {
        return honeyProducts;
    }

    public void setHoneyProducts(List<HoneyProduct> honeyProducts) {
        this.honeyProducts = honeyProducts;
    }

    public void addHoneyProduct(HoneyProduct honeyProduct) {
        honeyProducts.add(honeyProduct);
        honeyProduct.setHive(this);
    }

    public void removeHoneyProduct(HoneyProduct honeyProduct) {
        honeyProducts.remove(honeyProduct);
        honeyProduct.setHive(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hive hive = (Hive) o;
        return Objects.equals(hiveId, hive.hiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hiveId);
    }

    @Override
    public String toString() {
        return "Hive #" + hiveNumber + " (Queen: " + queenYear + ")";
    }
}