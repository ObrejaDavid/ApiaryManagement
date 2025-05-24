package org.apiary.service.impl;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.model.HoneyProduct;
import org.apiary.repository.interfaces.HoneyProductRepository;
import org.apiary.service.interfaces.ApiaryService;
import org.apiary.service.interfaces.HiveService;
import org.apiary.service.interfaces.HoneyProductService;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.EventManager;
import org.apiary.utils.pagination.Page;
import org.apiary.utils.pagination.Pageable;
import org.apiary.utils.pagination.PaginationUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HoneyProductServiceImpl extends EventManager<EntityChangeEvent<?>> implements HoneyProductService {

    private static final Logger LOGGER = Logger.getLogger(HoneyProductServiceImpl.class.getName());
    private final HoneyProductRepository honeyProductRepository;
    private final ApiaryService apiaryService;
    private final HiveService hiveService;

    public HoneyProductServiceImpl(HoneyProductRepository honeyProductRepository,
                                   ApiaryService apiaryService,
                                   HiveService hiveService) {
        this.honeyProductRepository = honeyProductRepository;
        this.apiaryService = apiaryService;
        this.hiveService = hiveService;
    }

    @Override
    public HoneyProduct createHoneyProduct(String name, String description, BigDecimal price,
                                           BigDecimal quantity, Apiary apiary, Hive hive,
                                           Beekeeper beekeeper) {
        try {
            // Check if apiary belongs to beekeeper
            if (!apiaryService.isApiaryOwnedByBeekeeper(beekeeper, apiary.getApiaryId())) {
                LOGGER.warning("Apiary does not belong to beekeeper: " +
                        apiary.getApiaryId() + ", " + beekeeper.getUsername());
                return null;
            }

            // Check if hive belongs to apiary (if provided)
            if (hive != null && !hive.getApiary().equals(apiary)) {
                LOGGER.warning("Hive does not belong to apiary: " +
                        hive.getHiveId() + ", " + apiary.getApiaryId());
                return null;
            }

            HoneyProduct product = new HoneyProduct(name, description, price, quantity, apiary);
            product.setHive(hive);

            HoneyProduct savedProduct = honeyProductRepository.save(product);

            // Notify observers
            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.CREATED, savedProduct));

            LOGGER.info("Created new honey product: " + name + " for apiary: " + apiary.getName());
            return savedProduct;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating honey product: " + name, e);
            return null;
        }
    }

    @Override
    public HoneyProduct updateHoneyProduct(Integer productId, String name, String description,
                                           BigDecimal price, BigDecimal quantity,
                                           Beekeeper beekeeper) {
        try {
            Optional<HoneyProduct> productOpt = honeyProductRepository.findById(productId);
            if (productOpt.isEmpty()) {
                LOGGER.warning("Honey product not found: " + productId);
                return null;
            }

            HoneyProduct product = productOpt.get();
            HoneyProduct oldProduct = new HoneyProduct(product.getName(), product.getDescription(),
                    product.getPrice(), product.getQuantity(), product.getApiary());
            oldProduct.setProductId(product.getProductId());
            oldProduct.setHive(product.getHive());

            // Check if product belongs to beekeeper
            if (!isProductOwnedByBeekeeper(productId, beekeeper)) {
                LOGGER.warning("Honey product does not belong to beekeeper: " +
                        productId + ", " + beekeeper.getUsername());
                return null;
            }

            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setQuantity(quantity);

            HoneyProduct updatedProduct = honeyProductRepository.save(product);

            // Notify observers
            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, updatedProduct, oldProduct));

            LOGGER.info("Updated honey product: " + productId);
            return updatedProduct;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating honey product: " + productId, e);
            return null;
        }
    }

    @Override
    public boolean deleteHoneyProduct(Integer productId, Beekeeper beekeeper) {
        try {
            Optional<HoneyProduct> productOpt = honeyProductRepository.findById(productId);
            if (productOpt.isEmpty()) {
                LOGGER.warning("Honey product not found: " + productId);
                return false;
            }

            HoneyProduct product = productOpt.get();

            // Check if product belongs to beekeeper
            if (!isProductOwnedByBeekeeper(productId, beekeeper)) {
                LOGGER.warning("Honey product does not belong to beekeeper: " +
                        productId + ", " + beekeeper.getUsername());
                return false;
            }

            honeyProductRepository.deleteById(productId);

            // Notify observers
            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.DELETED, product));

            LOGGER.info("Deleted honey product: " + productId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting honey product: " + productId, e);
            return false;
        }
    }

    // ... rest of the methods remain the same
    @Override
    public Optional<HoneyProduct> findById(Integer productId) {
        try {
            return honeyProductRepository.findById(productId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey product by ID: " + productId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<HoneyProduct> findAll() {
        try {
            return honeyProductRepository.findAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all honey products", e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByApiary(Apiary apiary) {
        try {
            return honeyProductRepository.findByApiary(apiary);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by apiary: " + apiary.getApiaryId(), e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByHive(Hive hive) {
        try {
            return honeyProductRepository.findByHive(hive);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by hive: " + hive.getHiveId(), e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByNameContaining(String name) {
        try {
            return honeyProductRepository.findByNameContaining(name);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by name: " + name, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceLessThan(BigDecimal price) {
        try {
            return honeyProductRepository.findByPriceLessThan(price);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price less than: " + price, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceGreaterThan(BigDecimal price) {
        try {
            return honeyProductRepository.findByPriceGreaterThan(price);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price greater than: " + price, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            return honeyProductRepository.findByPriceBetween(minPrice, maxPrice);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price between: " +
                    minPrice + " and " + maxPrice, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findAvailableProducts() {
        try {
            return honeyProductRepository.findAvailableProducts();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding available honey products", e);
            return List.of();
        }
    }

    @Override
    public boolean isProductOwnedByBeekeeper(Integer productId, Beekeeper beekeeper) {
        try {
            Optional<HoneyProduct> productOpt = honeyProductRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return false;
            }

            HoneyProduct product = productOpt.get();
            return apiaryService.isApiaryOwnedByBeekeeper(
                    beekeeper,
                    product.getApiary().getApiaryId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if honey product is owned by beekeeper: " +
                    productId + ", " + beekeeper.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean updateQuantityAfterPurchase(Integer productId, BigDecimal quantityToSubtract) {
        try {
            Optional<HoneyProduct> productOpt = honeyProductRepository.findById(productId);
            if (productOpt.isEmpty()) {
                LOGGER.warning("Honey product not found: " + productId);
                return false;
            }

            HoneyProduct product = productOpt.get();

            // Check if there is enough quantity
            if (product.getQuantity().compareTo(quantityToSubtract) < 0) {
                LOGGER.warning("Not enough quantity available for honey product: " + productId);
                return false;
            }

            // Update quantity
            product.setQuantity(product.getQuantity().subtract(quantityToSubtract));
            honeyProductRepository.save(product);
            LOGGER.info("Updated quantity for honey product: " + productId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating quantity for honey product: " + productId, e);
            return false;
        }
    }

    @Override
    public long countProductsByApiary(Integer apiaryId) {
        try {
            Optional<Apiary> apiaryOpt = apiaryService.findById(apiaryId);
            if (apiaryOpt.isEmpty()) {
                return 0;
            }
            return honeyProductRepository.findByApiary(apiaryOpt.get()).size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting products by apiary: " + apiaryId, e);
            return 0;
        }
    }

    @Override
    public long countProductsByHive(Integer hiveId) {
        try {
            Optional<Hive> hiveOpt = hiveService.findById(hiveId);
            if (hiveOpt.isEmpty()) {
                return 0;
            }
            return honeyProductRepository.findByHive(hiveOpt.get()).size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting products by hive: " + hiveId, e);
            return 0;
        }
    }

    @Override
    public List<HoneyProduct> findByBeekeeper(Beekeeper beekeeper) {
        try {
            List<Apiary> apiaries = apiaryService.findByBeekeeper(beekeeper);
            return apiaries.stream()
                    .flatMap(apiary -> honeyProductRepository.findByApiary(apiary).stream())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding products by beekeeper: " + beekeeper.getUsername(), e);
            return List.of();
        }
    }

    @Override
    public Page<HoneyProduct> findByFilters(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        try {
            List<HoneyProduct> allProducts = honeyProductRepository.findAll();

            // Apply filters
            List<HoneyProduct> filteredProducts = allProducts.stream()
                    .filter(product -> {
                        if (category != null && !product.getName().toLowerCase().contains(category.toLowerCase())) {
                            return false;
                        }
                        if (minPrice != null && product.getPrice().compareTo(minPrice) < 0) {
                            return false;
                        }
                        if (maxPrice != null && product.getPrice().compareTo(maxPrice) > 0) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            return PaginationUtils.createPage(filteredProducts, pageable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding products by filters", e);
            return new Page<>(List.of(), pageable.getPage(), pageable.getSize(), 0);
        }
    }

    @Override
    public Page<HoneyProduct> findAvailableProducts(Pageable pageable) {
        try {
            List<HoneyProduct> availableProducts = honeyProductRepository.findAvailableProducts();
            return PaginationUtils.createPage(availableProducts, pageable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding available products", e);
            return new Page<>(List.of(), pageable.getPage(), pageable.getSize(), 0);
        }
    }

    @Override
    public Page<HoneyProduct> findByNameContaining(String name, String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        try {
            List<HoneyProduct> allProducts = honeyProductRepository.findByNameContaining(name);

            // Apply additional filters
            List<HoneyProduct> filteredProducts = allProducts.stream()
                    .filter(product -> {
                        if (category != null && !product.getName().toLowerCase().contains(category.toLowerCase())) {
                            return false;
                        }
                        if (minPrice != null && product.getPrice().compareTo(minPrice) < 0) {
                            return false;
                        }
                        if (maxPrice != null && product.getPrice().compareTo(maxPrice) > 0) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            return PaginationUtils.createPage(filteredProducts, pageable);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding products by name containing: " + name, e);
            return new Page<>(List.of(), pageable.getPage(), pageable.getSize(), 0);
        }
    }
}