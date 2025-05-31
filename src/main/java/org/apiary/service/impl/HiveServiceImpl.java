package org.apiary.service.impl;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.repository.interfaces.HiveRepository;
import org.apiary.service.interfaces.ApiaryService;
import org.apiary.service.interfaces.HiveService;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.EventManager;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiveServiceImpl extends EventManager<EntityChangeEvent<?>> implements HiveService {

    private static final Logger LOGGER = Logger.getLogger(HiveServiceImpl.class.getName());
    private final HiveRepository hiveRepository;
    private final ApiaryService apiaryService;

    public HiveServiceImpl(HiveRepository hiveRepository, ApiaryService apiaryService) {
        this.hiveRepository = hiveRepository;
        this.apiaryService = apiaryService;
    }

    @Override
    public Hive createHive(Integer hiveNumber, Integer queenYear, Apiary apiary, Beekeeper beekeeper) {
        try {
            if (!apiaryService.isApiaryOwnedByBeekeeper(beekeeper, apiary.getApiaryId())) {
                LOGGER.warning("Apiary does not belong to beekeeper: " +
                        apiary.getApiaryId() + ", " + beekeeper.getUsername());
                return null;
            }

            List<Hive> existingHives = findByApiaryAndHiveNumber(apiary, hiveNumber);
            if (!existingHives.isEmpty()) {
                LOGGER.warning("Hive number already exists in apiary: " +
                        hiveNumber + ", " + apiary.getApiaryId());
                return null;
            }

            Hive hive = new Hive(hiveNumber, queenYear, apiary);
            Hive savedHive = hiveRepository.save(hive);

            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.CREATED, savedHive));

            LOGGER.info("Created new hive: " + hiveNumber + " in apiary: " + apiary.getName());
            return savedHive;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating hive: " + hiveNumber, e);
            return null;
        }
    }

    @Override
    public Hive updateHive(Integer hiveId, Integer hiveNumber, Integer queenYear, Beekeeper beekeeper) {
        try {
            Optional<Hive> hiveOpt = hiveRepository.findById(hiveId);
            if (hiveOpt.isEmpty()) {
                LOGGER.warning("Hive not found: " + hiveId);
                return null;
            }

            Hive hive = hiveOpt.get();
            Hive oldHive = new Hive(hive.getHiveNumber(), hive.getQueenYear(), hive.getApiary());
            oldHive.setHiveId(hive.getHiveId());

            if (!isHiveOwnedByBeekeeper(hiveId, beekeeper)) {
                LOGGER.warning("Hive does not belong to beekeeper: " +
                        hiveId + ", " + beekeeper.getUsername());
                return null;
            }

            if (!hive.getHiveNumber().equals(hiveNumber)) {
                List<Hive> existingHives = findByApiaryAndHiveNumber(hive.getApiary(), hiveNumber);
                if (!existingHives.isEmpty()) {
                    LOGGER.warning("Hive number already exists in apiary: " +
                            hiveNumber + ", " + hive.getApiary().getApiaryId());
                    return null;
                }
            }

            hive.setHiveNumber(hiveNumber);
            hive.setQueenYear(queenYear);

            Hive updatedHive = hiveRepository.save(hive);

            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, updatedHive, oldHive));

            LOGGER.info("Updated hive: " + hiveId);
            return updatedHive;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating hive: " + hiveId, e);
            return null;
        }
    }

    @Override
    public boolean deleteHive(Integer hiveId, Beekeeper beekeeper) {
        try {
            Optional<Hive> hiveOpt = hiveRepository.findById(hiveId);
            if (hiveOpt.isEmpty()) {
                LOGGER.warning("Hive not found: " + hiveId);
                return false;
            }

            Hive hive = hiveOpt.get();

            if (!isHiveOwnedByBeekeeper(hiveId, beekeeper)) {
                LOGGER.warning("Hive does not belong to beekeeper: " +
                        hiveId + ", " + beekeeper.getUsername());
                return false;
            }

            hiveRepository.deleteById(hiveId);

            notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.DELETED, hive));

            LOGGER.info("Deleted hive: " + hiveId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting hive: " + hiveId, e);
            return false;
        }
    }

    @Override
    public Optional<Hive> findById(Integer hiveId) {
        try {
            return hiveRepository.findById(hiveId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hive by ID: " + hiveId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Hive> findByApiary(Apiary apiary) {
        try {
            return hiveRepository.findByApiary(apiary);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by apiary: " + apiary.getApiaryId(), e);
            return List.of();
        }
    }

    @Override
    public List<Hive> findByApiaryAndHiveNumber(Apiary apiary, Integer hiveNumber) {
        try {
            return hiveRepository.findByApiaryAndHiveNumber(apiary, hiveNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by apiary and hive number: " +
                    apiary.getApiaryId() + ", " + hiveNumber, e);
            return List.of();
        }
    }

    @Override
    public List<Hive> findByQueenYear(Integer queenYear) {
        try {
            return hiveRepository.findByQueenYear(queenYear);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by queen year: " + queenYear, e);
            return List.of();
        }
    }

    @Override
    public long countByApiary(Apiary apiary) {
        try {
            return hiveRepository.countByApiary(apiary);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting hives by apiary: " + apiary.getApiaryId(), e);
            return 0;
        }
    }

    @Override
    public boolean isHiveOwnedByBeekeeper(Integer hiveId, Beekeeper beekeeper) {
        try {
            Optional<Hive> hiveOpt = hiveRepository.findById(hiveId);
            if (hiveOpt.isEmpty()) {
                return false;
            }

            Hive hive = hiveOpt.get();
            return apiaryService.isApiaryOwnedByBeekeeper(
                    beekeeper,
                    hive.getApiary().getApiaryId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if hive is owned by beekeeper: " +
                    hiveId + ", " + beekeeper.getUsername(), e);
            return false;
        }
    }
}