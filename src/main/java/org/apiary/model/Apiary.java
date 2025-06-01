package org.apiary.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Apiary")
public class Apiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apiaryId")
    private Integer apiaryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @ManyToOne
    @JoinColumn(name = "beekeeperId", nullable = false)
    private Beekeeper beekeeper;

    @OneToMany(mappedBy = "apiary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Hive> hives = new ArrayList<>();

    @OneToMany(mappedBy = "apiary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HoneyProduct> honeyProducts = new ArrayList<>();

    public Apiary() {}

    public Apiary(String name, String location, Beekeeper beekeeper) {
        this.name = name;
        this.location = location;
        this.beekeeper = beekeeper;
    }

    public Integer getApiaryId() {
        return apiaryId;
    }

    public void setApiaryId(Integer apiaryId) {
        this.apiaryId = apiaryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Beekeeper getBeekeeper() {
        return beekeeper;
    }

    public void setBeekeeper(Beekeeper beekeeper) {
        this.beekeeper = beekeeper;
    }

    public List<Hive> getHives() {
        return hives;
    }

    public void setHives(List<Hive> hives) {
        this.hives = hives;
    }

    public void addHive(Hive hive) {
        hives.add(hive);
        hive.setApiary(this);
    }

    public void removeHive(Hive hive) {
        hives.remove(hive);
        hive.setApiary(null);
    }

    public List<HoneyProduct> getHoneyProducts() {
        return honeyProducts;
    }

    public void setHoneyProducts(List<HoneyProduct> honeyProducts) {
        this.honeyProducts = honeyProducts;
    }

    public void addHoneyProduct(HoneyProduct honeyProduct) {
        honeyProducts.add(honeyProduct);
        honeyProduct.setApiary(this);
    }

    public void removeHoneyProduct(HoneyProduct honeyProduct) {
        honeyProducts.remove(honeyProduct);
        honeyProduct.setApiary(null);
    }

    public String viewApiaryData() {
        return "Apiary {" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", number of hives=" + hives.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Apiary apiary = (Apiary) o;
        return Objects.equals(apiaryId, apiary.apiaryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiaryId);
    }

    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}