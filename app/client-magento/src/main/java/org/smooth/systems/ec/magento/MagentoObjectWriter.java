package org.smooth.systems.ec.magento;

import java.io.File;
import java.util.List;

import org.smooth.systems.ec.client.api.MigrationSystemWriter;
import org.smooth.systems.ec.client.util.ObjectIdMapper;
import org.smooth.systems.ec.exceptions.NotImplementedException;
import org.smooth.systems.ec.exceptions.ObjectAlreadyExistsException;
import org.smooth.systems.ec.migration.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "migration.magento2", name = "base-url")
public class MagentoObjectWriter extends AbstractMagentoConnector implements MigrationSystemWriter {

  public MagentoObjectWriter(Magento2ConnectorConfiguration config) {
    super(config);
  }

  @Override
  public void writeUsers(List<User> customers) throws ObjectAlreadyExistsException {
    throw new NotImplementedException();
  }

  @Override
  public void writeUsers(List<User> customers, boolean updateIfExists) throws ObjectAlreadyExistsException {
    throw new NotImplementedException();
  }

  @Override
  public void writeCategories(Category categories) throws ObjectAlreadyExistsException {
    throw new NotImplementedException();
  }

  @Override
  public void writeCategories(Category categories, boolean updateIfExists) throws ObjectAlreadyExistsException {
    throw new NotImplementedException();
  }

  @Override
  public ObjectIdMapper getCategoriesObjectIdMapper() {
    throw new NotImplementedException();
  }

  @Override
  public ObjectIdMapper getProductsObjectIdMapper() {
    throw new NotImplementedException();
  }

	@Override
	public Product writeProduct(Product product) {
    throw new NotImplementedException();
	}

	@Override
	public void uploadProductImages(Long prodId, File productImage) {
    throw new NotImplementedException();
	}

  @Override
  public Manufacturer writeManufacturer(String manufacturerName) {
    throw new NotImplementedException();
  }

  @Override
  public void writeProductPriceTier(ProductPriceStrategies priceStrategy) {
    throw new NotImplementedException();
  }
}