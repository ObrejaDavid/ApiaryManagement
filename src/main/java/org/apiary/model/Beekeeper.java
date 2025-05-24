package org.apiary.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a beekeeper user who manages apiaries and hives
 */
@Entity
@DiscriminatorValue("BEEKEEPER")
public class Beekeeper extends User {

    @OneToMany(mappedBy = "beekeeper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Apiary> apiaries = new ArrayList<>();

    // Additional beekeeper-specific fields can be added here
    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "experience")
    private Integer yearsOfExperience;

    // Default constructor required by JPA
    public Beekeeper() {}

    public Beekeeper(String username, String password) {
        super(username, password);
    }

    public Beekeeper(String username, String password, String phone, String address) {
        super(username, password);
        this.phone = phone;
        this.address = address;
    }

    // Getters and setters
    public List<Apiary> getApiaries() {
        return apiaries;
    }

    public void setApiaries(List<Apiary> apiaries) {
        this.apiaries = apiaries;
    }

    public void addApiary(Apiary apiary) {
        apiaries.add(apiary);
        apiary.setBeekeeper(this);
    }

    public void removeApiary(Apiary apiary) {
        apiaries.remove(apiary);
        apiary.setBeekeeper(null);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    /**
     * Add a new hive to an existing apiary
     * @param apiary The apiary to add the hive to
     * @param hiveNumber The hive number
     * @param queenYear The year the queen was introduced (optional)
     * @return The created hive
     */
    public Hive addHive(Apiary apiary, Integer hiveNumber, Integer queenYear) {
        if (!apiaries.contains(apiary)) {
            throw new IllegalArgumentException("Cannot add hive to apiary that doesn't belong to this beekeeper");
        }

        Hive hive = new Hive(hiveNumber, queenYear, apiary);
        apiary.addHive(hive);
        return hive;
    }

    /**
     * Delete a hive from an apiary
     * @param hive The hive to delete
     */
    public void deleteHive(Hive hive) {
        Apiary apiary = hive.getApiary();
        if (!apiaries.contains(apiary)) {
            throw new IllegalArgumentException("Cannot delete hive from apiary that doesn't belong to this beekeeper");
        }

        apiary.removeHive(hive);
    }

    /**
     * Modify an existing hive
     * @param hive The hive to modify
     * @param newHiveNumber The new hive number
     * @param newQueenYear The new queen year
     */
    public void modifyHive(Hive hive, Integer newHiveNumber, Integer newQueenYear) {
        Apiary apiary = hive.getApiary();
        if (!apiaries.contains(apiary)) {
            throw new IllegalArgumentException("Cannot modify hive from apiary that doesn't belong to this beekeeper");
        }

        hive.setHiveNumber(newHiveNumber);
        hive.setQueenYear(newQueenYear);
    }

    /**
     * Add a honey product for a specific hive
     * @param hive The hive producing the honey
     * @param name The name of the honey product
     * @param description The description of the honey product
     * @param price The price of the honey product
     * @param quantity The available quantity
     * @return The created honey product
     */
    public HoneyProduct addHoneyProduct(Hive hive, String name, String description,
                                        java.math.BigDecimal price, java.math.BigDecimal quantity) {
        Apiary apiary = hive.getApiary();
        if (!apiaries.contains(apiary)) {
            throw new IllegalArgumentException("Cannot add honey product to hive that doesn't belong to this beekeeper");
        }

        HoneyProduct product = new HoneyProduct(name, description, price, quantity, apiary);
        product.setHive(hive);
        hive.addHoneyProduct(product);
        return product;
    }

    @Override
    public String toString() {
        return "Beekeeper: " + getUsername() + " (Apiaries: " + apiaries.size() + ")";
    }
}