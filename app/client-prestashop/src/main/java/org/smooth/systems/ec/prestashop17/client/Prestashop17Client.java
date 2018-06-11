package org.smooth.systems.ec.prestashop17.client;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.smooth.systems.ec.prestashop17.component.PrestashopLanguageTranslatorCache;
import org.smooth.systems.ec.prestashop17.model.Category;
import org.smooth.systems.ec.prestashop17.model.ImageUploadResponse;
import org.smooth.systems.ec.prestashop17.model.ImageUploadResponse.UploadedImage;
import org.smooth.systems.ec.prestashop17.model.Language;
import org.smooth.systems.ec.prestashop17.model.Manufacturer;
import org.smooth.systems.ec.prestashop17.model.Product;
import org.smooth.systems.ec.prestashop17.model.Tag;
import org.smooth.systems.ec.prestashop17.util.Prestashop17ClientUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Prestashop17Client {

  public static final String URL_PRODUCT_IMAGE = "/images/products/%d";

  public static final String URL_TAGS = "/tags";
  public static final String URL_TAG = "/tags/%d";

  public static final String URL_PRODUCTS = "/products";
  public static final String URL_PRODUCT = "/products/%d";

  public static final String URL_MANUFACTURERS = "/manufacturers";
  public static final String URL_MANUFACTURER = "/manufacturers/%d";

  private final String baseUrl;

  private final RestTemplate client;

  public Prestashop17Client(String baseUrl, String authToken) {
    Assert.notNull(authToken, "authToken is null");
    this.baseUrl = baseUrl;
    client = new RestTemplate();
    client.getInterceptors().add(new BasicAuthorizationInterceptor(authToken, "invalid"));
    log.info("Initialized client for url: {}", baseUrl);
  }

  public RestTemplate getClient() {
    return client;
  }

  public List<Language> getLanguages() {
    log.info("getLanguages()");

    ResponseEntity<Languages> response = client.getForEntity(baseUrl + "/languages/", Languages.class);
    Languages languages = response.getBody();
    List<Language> res = new ArrayList<>();
    for (ObjectRefId langRef : languages.getLanguages()) {
      ResponseEntity<LanguageWrapper> responseLang = client.getForEntity(baseUrl + "/languages/" + langRef.getId(), LanguageWrapper.class);
      res.add(responseLang.getBody().getLanguage());
    }
    return res;
  }

  public List<Tag> getTags() {
    String tagsUrl = baseUrl + URL_TAGS;
    log.info("getTags({})", tagsUrl);
    ResponseEntity<Tags> response = client.getForEntity(tagsUrl, Tags.class);
    Tags tags = response.getBody();
    List<Tag> res = new ArrayList<>();
    for (ObjectRefId tagRef : tags.getTags()) {
      String tagUrl = baseUrl + String.format(URL_TAG, tagRef.getId());
      ResponseEntity<TagWrapper> responseLang = client.getForEntity(tagUrl, TagWrapper.class);
      res.add(responseLang.getBody().getTag());
    }
    return res;
  }

  public Long createNewTag(Long langId, String tagName) {
    log.debug("createNewTag({}, {})", langId, tagName);
    TagWrapper wrapper = new TagWrapper();
    wrapper.setTag(Tag.builder().name(tagName).idLang(langId).build());
    String tagUrl = baseUrl + URL_TAGS;
    ResponseEntity<String> response = client.postForEntity(tagUrl, wrapper, String.class);
    log.info("Created tag: {}", response.getBody());
    return 0L;
  }

  public List<ObjectRefId> getCategoriesMetaData() {
    log.debug("getCategoriesMetaData()");
    ResponseEntity<Categories> response = client.getForEntity(baseUrl + "/categories/", Categories.class);
    Categories categories = response.getBody();
    log.trace("Retrieved {} category references", categories.getCategories().size());
    return categories.getCategories();
  }

  public List<Category> getCategories() {
    log.debug("getCategories()");
    List<ObjectRefId> categoryRefs = getCategoriesMetaData();
    log.trace("Retrieved {} categories", categoryRefs.size());

    List<Category> resCategory = new ArrayList<>();
    for (ObjectRefId catRef : categoryRefs) {
      Category category = getCategory(catRef.getId());
      resCategory.add(category);
    }
    log.trace("Retrieved {} categories", resCategory.size());
    return resCategory;
  }

  public Category getCategory(Long categoryId) {
    ResponseEntity<CategoryWrapper> response = client.getForEntity(baseUrl + "/categories/" + categoryId, CategoryWrapper.class);
    CategoryWrapper categoryWrapper = response.getBody();
    return categoryWrapper.getCategory();
  }

  public void removeCategory(Long categoryId) {
    client.delete(baseUrl + "/categories/" + categoryId);
  }

  public Product getProduct(Long productId) {
    String url = baseUrl + String.format(URL_PRODUCT, productId);
    log.debug("getProduct({})", url);
    ResponseEntity<ProductWrapper> response = client.getForEntity(url, ProductWrapper.class);
    ProductWrapper categoryWrapper = response.getBody();
    return categoryWrapper.getProduct();
  }

  public Category writeCategory(PrestashopLanguageTranslatorCache languagesCache, Category category) {
    log.debug("writeCategory({})", category);
    CategoryWrapper catWrapper = new CategoryWrapper();
    catWrapper.setCategory(category);
    catWrapper.getCategory().setId(null);

    Prestashop17ClientUtil.fillUpEmptyAttributesInCategory(languagesCache, category);

    String requestBody = Prestashop17ClientUtil.convertToUTF8(objectToString(catWrapper, CategoryWrapper.class));
    log.info("Request:\n{}", requestBody);
    ResponseEntity<CategoryWrapper> response = client.postForEntity(baseUrl + "/categories", requestBody, CategoryWrapper.class);
    Category postedCategory = response.getBody().getCategory();
    log.info("Wrote category: {}", postedCategory);
    return postedCategory;
  }

  public Product writeProduct(Product product) {
    log.debug("writeProduct({})", product);
    product.setId(null);
    ProductWrapper productWrapper = new ProductWrapper();
    productWrapper.setProduct(product);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

    printProduct(productWrapper);

    String requestContent = Prestashop17ClientUtil.convertToUTF8(productToString(productWrapper));
    ResponseEntity<ProductWrapper> response = client.postForEntity(baseUrl + URL_PRODUCTS, requestContent, ProductWrapper.class);
    Product postedProduct = response.getBody().getProduct();
    log.info("Wrote product: {}", postedProduct);
    product.setId(postedProduct.getId());
    return product;
  }

  public void deleteProduct(Long productId) {
    log.debug("deleteProduct({})", productId);
    client.delete(baseUrl + String.format(URL_PRODUCT, productId));
    log.info("Removed product with id: {}", productId);
  }

  public UploadedImage uploadProductImage(Long productId, File imageUrl) {
    Assert.notNull(productId, "productId is null");
    Assert.notNull(imageUrl, "imageUrl is null");

    String urlImageUpload = getProductImageUrl(productId);
    log.debug("uploadProductImage({}, {})", urlImageUpload, imageUrl.getAbsolutePath());

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("image", new FileSystemResource(imageUrl));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<LinkedMultiValueMap<String, Object>>(map, headers);
    ResponseEntity<ImageUploadResponse> result = client.exchange(urlImageUpload, HttpMethod.POST, request, ImageUploadResponse.class);
    log.info("Uploaded image: {}", result.getBody());
    return result.getBody().getUploadedImage();
  }

  public List<Manufacturer> getAllManufacturers() {
    log.debug("getAllManufacturers()");
    ResponseEntity<Manufacturers> response = client.getForEntity(baseUrl + "/manufacturers", Manufacturers.class);
    Manufacturers manufacturers = response.getBody();

    List<Manufacturer> res = new ArrayList<>();
    for (ObjectRefId langRef : manufacturers.getManufacturers()) {
      res.add(getManufacturer(langRef.getId()));
    }
    return res;
  }

  public Manufacturer getManufacturer(Long manufacturerId) {
    Assert.notNull(manufacturerId, "manufacturerId is null");
    String url = String.format(URL_MANUFACTURER, manufacturerId);
    ResponseEntity<ManufacturerWrapper> responseManufacturers = client.getForEntity(baseUrl + url, ManufacturerWrapper.class);
    return responseManufacturers.getBody().getManufacturer();
  }

  public Manufacturer writeManufacturer(Manufacturer manufacturer) {
    Assert.notNull(manufacturer, "manufacturer is null");
    log.debug("writeManufacturer({})", manufacturer);

    manufacturer.setId(null);
    ManufacturerWrapper manufacturerWrapper = new ManufacturerWrapper();
    manufacturerWrapper.setManufacturer(manufacturer);

    String requestContent = objectToString(manufacturerWrapper, ManufacturerWrapper.class);
    ResponseEntity<ManufacturerWrapper> response = client.postForEntity(baseUrl + URL_MANUFACTURERS, requestContent,
        ManufacturerWrapper.class);
    Manufacturer postedManufacturer = response.getBody().getManufacturer();
    log.info("Wrote manufacturer: {}", postedManufacturer);
    return postedManufacturer;
  }

  private <T> String objectToString(T objectWrapper, Class<T> clazz) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      StringWriter sw = new StringWriter();
      jaxbMarshaller.marshal(objectWrapper, sw);
      return sw.toString();
    } catch (Exception e) {
      log.error("Error while marshalling class: {}", ProductWrapper.class.getName(), e);
      throw new RuntimeException(e);
    }
  }

  public void printProduct(ProductWrapper prodWrapper) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ProductWrapper.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.marshal(prodWrapper, System.out);
    } catch (Exception e) {
      log.error("Error while marshalling class: {}", ProductWrapper.class.getName(), e);
    }
  }

  public String productToString(ProductWrapper prodWrapper) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ProductWrapper.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      StringWriter sw = new StringWriter();
      jaxbMarshaller.marshal(prodWrapper, sw);
      return sw.toString();
    } catch (Exception e) {
      log.error("Error while marshalling class: {}", ProductWrapper.class.getName(), e);
      throw new RuntimeException(e);
    }
  }

  private String getProductImageUrl(Long productId) {
    return String.format(baseUrl + URL_PRODUCT_IMAGE, productId);
  }
}
