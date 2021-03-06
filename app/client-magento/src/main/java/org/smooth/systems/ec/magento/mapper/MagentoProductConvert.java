package org.smooth.systems.ec.magento.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.smooth.systems.ec.migration.model.Product;
import org.smooth.systems.ec.migration.model.Product.ProductType;
import org.smooth.systems.ec.migration.model.Product.ProductVisibility;
import org.smooth.systems.ec.migration.model.ProductTranslateableAttributes;
import org.smooth.systems.utils.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.github.chen0040.magento.MagentoClient;
import com.github.chen0040.magento.models.MagentoAttribute;
import com.github.chen0040.magento.models.ProductMedia;

@Component
@ConditionalOnProperty(prefix = "migration.magento2", name = "base-url")
public class MagentoProductConvert {

  public static final String KEY_NAME_DESCRIPTION = "description";
  public static final String KEY_NAME_FRIENDLY_URL = "url_key";
  public static final String KEY_NAME_SHORT_DESCRIPTION = "short_description";
  public static final String KEY_NAME_TAGS = "meta_keyword";

  public static final String KEY_NAME_IMAGE = "image";

  // not used yet
  public static final String KEY_NAME_MANUFACTURER = "manufacturer";
  public static final String KEY_NAME_META_TITLE = "meta_title";
  public static final String KEY_NAME_META_DESCRIPTION = "meta_description";
  public static final String KEY_NAME_URL_PATH = "url_path";

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final MagentoClient client;

  @Autowired
  public MagentoProductConvert(MagentoClient client) {
    this.client = client;
  }

  public Product convertProduct(com.github.chen0040.magento.models.Product product, String language, boolean appendImageUrls) {
    Assert.notNull(product, "product is null, unable to convert");

    Product prod = new Product();
    prod.setId(product.getId());
    prod.setSku(product.getSku());
    prod.setCostPrice(product.getPrice());
    prod.setCreationDate(LocalDateTime.parse(product.getUpdated_at(), DATE_FORMATTER));
    // prod.setSalesPrice(salesPrice);
    prod.setType(ProductType.Simple);
    prod.setVisibility(ProductVisibility.Everywhere);

    // prod.setAttributes(attributes);
    // prod.setCategories(categories);
    // prod.setDimension(dimension);
    // prod.setProductImageUrls(productImageUrls);
    // populateChildCategory(cat, category.getChildren_data(), language);

    populateProductAttributes(product, prod, language);
    if(appendImageUrls) {
      appendImageUrlsToProduct(product.getSku(), prod);
    }
    return prod;
  }

  private void populateProductAttributes(com.github.chen0040.magento.models.Product product, Product prod, String language) {
    ProductTranslateableAttributes attributes = new ProductTranslateableAttributes(language);
    attributes.setName(product.getName());
    attributes.setDescription(getMagentoAttributeValue(KEY_NAME_DESCRIPTION, product));
    attributes.setFriendlyUrl(getMagentoAttributeValue(KEY_NAME_FRIENDLY_URL, product));
    attributes.setShortDescription(getMagentoAttributeValue(KEY_NAME_SHORT_DESCRIPTION, product));
    attributes.setTags(getMagentoAttributeListValue(KEY_NAME_TAGS, product));
    prod.getAttributes().add(attributes);
  }

  private void appendImageUrlsToProduct(String sku, Product prod) {
    List<ProductMedia> mediaList = client.media().getProductMediaList(prod.getSku());
    for (ProductMedia productMedia : mediaList) {
      if(!KEY_NAME_IMAGE.equals(productMedia.getMedia_type())) {
        continue;
      }
      prod.getProductImageUrls().add(productMedia.getFile());
    }
  }

  private String getMagentoAttributeValue(String attributeCode, com.github.chen0040.magento.models.Product product) {
    Assert.notNull(attributeCode, "attributeCode is null, unable to convert");
    List<MagentoAttribute> attributes = product.getCustom_attributes();
    Optional<MagentoAttribute> possibleAttribute = attributes.stream().filter(attr -> attributeCode.equals(attr.getAttribute_code()))
        .findFirst();
    if (possibleAttribute.isPresent()) {
      return (String) possibleAttribute.get().getValue();
    }
    return ErrorUtil
        .throwAndLog(String.format("Unable to retrieve attribute '%s' in product with sku '%s'", attributeCode, product.getSku()));
  }

  private List<String> getMagentoAttributeListValue(String attributeCode, com.github.chen0040.magento.models.Product product) {
    String listAsString = getMagentoAttributeValue(attributeCode, product);
    String[] tokens = listAsString.split(",");
    List<String> resList = new ArrayList<>();
    for (String token : tokens) {
      resList.add(token.trim());
    }
    return resList;
  }
}
