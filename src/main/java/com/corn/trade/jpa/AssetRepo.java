package com.corn.trade.jpa;

import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

public class AssetRepo extends JpaRepo<Asset, Long> {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetRepo.class);

	public AssetRepo() {
		super(Asset.class);
	}

	public Optional<Asset> findAsset(String assetName, Exchange exchange) throws DBException {
		try {
			return executeWithEntityManager(em -> {
				TypedQuery<Asset> query =
						em.createQuery("SELECT a FROM Asset a where a.name=:name and a.exchange=:exchange", Asset.class);
				query.setParameter("name", assetName);
				query.setParameter("exchange", exchange);
				return Optional.of(query.getSingleResult());
			});
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (PersistenceException e) {
			log.error("Error finding all {} : {}", e, e.getMessage());
			throw new DBException(e);
		}
	}
}
