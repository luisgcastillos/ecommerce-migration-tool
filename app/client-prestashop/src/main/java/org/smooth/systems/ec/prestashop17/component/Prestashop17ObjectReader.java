package org.smooth.systems.ec.prestashop17.component;

import java.util.List;
import java.util.stream.Collectors;

import org.smooth.systems.ec.client.api.MigrationClientConstants;
import org.smooth.systems.ec.client.api.MigrationSystemReader;

import org.smooth.systems.ec.migration.model.Category;
import org.smooth.systems.ec.migration.model.Product;
import org.smooth.systems.ec.migration.model.IProductMetaData;
import org.smooth.systems.ec.migration.model.User;
import org.smooth.systems.ec.prestashop17.api.Prestashop17Constants;
import org.smooth.systems.ec.prestashop17.client.Prestashop17Client;
import org.smooth.systems.ec.prestashop17.util.Prestashop17ProductConverter;

import org.smooth.systems.ec.client.api.SimpleCategory;
import org.smooth.systems.ec.client.api.ProductId;
import org.smooth.systems.ec.configuration.MigrationConfiguration;
import org.smooth.systems.ec.exceptions.NotImplementedException;
import org.smooth.systems.ec.migration.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = Prestashop17Constants.PRESTASHOP17_CONFIG_PREFIX, name = MigrationClientConstants.MIGRATION_CLIENT_BASE_URL)
public class Prestashop17ObjectReader extends AbstractPrestashop17Connector implements MigrationSystemReader {

  @Autowired
  public Prestashop17ObjectReader(MigrationConfiguration config, Prestashop17Client client) {
    super(config, client);
  }

  @Override
  public List<User> readAllUsers() {
    log.debug("readAllUsers()");
    List<User> users = readWebUsers();
    users.addAll(readAdministrativeUsers());
    return users;
  }

  @Override
  public List<User> readWebUsers() {
    log.debug("readWebUsers()");
    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public List<User> readAdministrativeUsers() {
    log.debug("readAdministrativeUsers()");
    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public List<Category> readAllCategories(List<SimpleCategory> categories) {
    log.debug("readAllCategories({})", categories);

    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public List<Product> readAllProductsForCategories(List<SimpleCategory> categories) {
    log.debug("readAllProductsForCategories({})", categories);
    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public List<Product> readAllProducts(List<ProductId> products) {
    log.debug("readAllProducts({})", products);
    throw new RuntimeException("Not implemented yet");
  }

  @Override
  public List<Manufacturer> readAllManufacturers() {
    log.debug("readAllManufacturers()");
    List<org.smooth.systems.ec.prestashop17.model.Manufacturer> manufacturers = client.getAllManufacturers();
    return manufacturers.stream().map(manufacturer -> manufacturer.convert()).collect(Collectors.toList());
  }

  @Override
  public List<ProductPriceStrategies> readProductsPriceStrategies(List<ProductId> products) {
    log.info("readProductsPriceStrategies({})", products);
    throw new NotImplementedException();
  }

  @Override
  public ProductPriceStrategies readProductPriceStrategies(Long productId) {
    log.info("readProductPriceStrategies({})", productId);
    throw new NotImplementedException();
  }

	@Override
	public List<IProductMetaData> readAllProductsMetaData() {
		log.debug("readAllProductsMetaData()");
		List<org.smooth.systems.ec.prestashop17.model.Product> products = client.getAllProducts();
		return products.stream().map(prod -> Prestashop17ProductConverter.convertFromPrestashop17Product(prod)).collect(Collectors.toList());
	}

  @Override
  public  List<Long> readAllProductSpecificPrices() {
    throw new NotImplementedException();
  }
}