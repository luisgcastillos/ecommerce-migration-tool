package org.smooth.systems.ec.prestashop17.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;
import org.smooth.systems.ec.prestashop17.model.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;
import org.smooth.systems.ec.prestashop17.util.Prestashop17ClientUtil;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

@Slf4j
public class Prestashop17ClientDeserializeTest {

  @Test
  public void deserializeLanguagesResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/languages_response_prestashop_1-7-3.xml");
    Languages languages = xmlMapper.readValue(inputFile, Languages.class);
    log.info("Languages: {}", languages);
  }

  @Test
  public void deserializeLanguageResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/language_response_prestashop_1-7-3.xml");
    LanguageWrapper language = xmlMapper.readValue(inputFile, LanguageWrapper.class);
    log.info("LanguageWrapper: {}", language);

    assertNotNull(language.getLanguage());
    Language lang = language.getLanguage();
    assertEquals(new Long(1), lang.getId());
    assertEquals("English (English)", lang.getName());
    assertEquals("en", lang.getIsoCode());
  }

  @Test
  public void deserializeTagResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/tag_response_prestashop_1-7-3.xml");
    TagWrapper tag = xmlMapper.readValue(inputFile, TagWrapper.class);
    log.info("Languages: {}", tag);
    assertNotNull(tag.getTag());
    assertEquals(new Long(56), tag.getTag().getId());
    assertEquals(new Long(57), tag.getTag().getIdLang());
    assertEquals("test134", tag.getTag().getName());
  }

  @Test
  public void deserializeTagsResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/tags_response_prestashop_1-7-3.xml");
    Tags tags = xmlMapper.readValue(inputFile, Tags.class);
    log.info("Languages: {}", tags);
     assertNotNull(tags.getTags());
     assertEquals(2, tags.getTags().size());
     assertEquals(new Long(1), tags.getTags().get(0).getId());
     assertEquals(new Long(2), tags.getTags().get(1).getId());
  }

  @Test
  public void deserializeCategoriesResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/categories_response_prestashop_1-7-3.xml");
    Categories categories = xmlMapper.readValue(inputFile, Categories.class);
    log.info("Categories: {}", categories);
    assertNotNull(categories.getCategories());
    assertEquals(70, categories.getCategories().size());
    assertEquals(new Long(1), categories.getCategories().get(0).getId());
    assertEquals(new Long(108), categories.getCategories().get(10).getId());
  }

  @Test
  public void deserializeImageUploadResponse() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/upload_product_image_prestashop_1-7-3.xml");
    ImageUploadResponse imageUploadResponse = xmlMapper.readValue(inputFile, ImageUploadResponse.class);
    log.info("ImageUploadResponse: {}", imageUploadResponse);
    assertNotNull(imageUploadResponse.getUploadedImage());
    assertEquals(new Long(4), imageUploadResponse.getUploadedImage().getId());
    assertEquals(new Long(2), imageUploadResponse.getUploadedImage().getProductId());
    assertEquals(new Long(3), imageUploadResponse.getUploadedImage().getPosition());
  }

  @Test
  public void deserializeProductSpecificPrices() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/product_specific_prices_1-7-3.xml");
    ProductSpecificPrices productSpcificPricesResponse = xmlMapper.readValue(inputFile, ProductSpecificPrices.class);
    log.info("ProductSpecificPrices: {}", productSpcificPricesResponse);
    assertNotNull(productSpcificPricesResponse.getSpecificPrices());
    assertEquals(4,productSpcificPricesResponse.getSpecificPrices().size());
    assertEquals(Long.valueOf(1),productSpcificPricesResponse.getSpecificPrices().get(0).getId());
    assertEquals(Long.valueOf(2),productSpcificPricesResponse.getSpecificPrices().get(1).getId());
    assertEquals(Long.valueOf(3),productSpcificPricesResponse.getSpecificPrices().get(2).getId());
    assertEquals(Long.valueOf(4),productSpcificPricesResponse.getSpecificPrices().get(3).getId());
  }

  @Test
  public void deserializeProductSpecificPrice() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/product_specific_price_1-7-3.xml");
    ProductSpecificPriceWrapper productSpcificPriceResponse = xmlMapper.readValue(inputFile, ProductSpecificPriceWrapper.class);
    log.info("ProductSpecificPrices: {}", productSpcificPriceResponse);
    assertNotNull(productSpcificPriceResponse.getSpecificPrice());
    ProductSpecificPrice specificPrice = productSpcificPriceResponse.getSpecificPrice();
    assertEquals(Long.valueOf(1021), specificPrice.getProductId());
    assertEquals(Long.valueOf(6), specificPrice.getQuantity());
    assertEquals(Double.valueOf("2.5"), specificPrice.getReduction());
    assertEquals(Long.valueOf(0), specificPrice.getReductionTax());
    assertEquals("amount", specificPrice.getReductionType());
  }

  @Test
  public void deserializeProductMetaData() throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    File inputFile = new File("src/test/resources/example_responses/products_list_1-7-3.xml");
    Products productResponse = xmlMapper.readValue(inputFile, Products.class);
    log.info("Products: {}", productResponse);
    Assert.notEmpty(productResponse.getProductRefs(), "Products references are empty.");
    assertEquals(1025L,productResponse.getProductRefs().size());
    assertEquals(new Long(270),productResponse.getProductRefs().get(0).getId());
    assertEquals("http://prestashop.local/api/products/270",productResponse.getProductRefs().get(0).getHref());
  }

	@Test
	public void deserializeCompleteProduct() throws IOException {
		XmlMapper xmlMapper = new XmlMapper();
		InputStream is = Prestashop17ClientDeserializeTest.class.getResourceAsStream("/example_responses/product_complete_production_1-7-3.xml");
		String fileAsString = convert(is);
		fileAsString = fileAsString.replaceAll("<br />", "");
		fileAsString = fileAsString.replaceAll("<br/>", "");
		CompleteProductWrapper product = xmlMapper.readValue(fileAsString, CompleteProductWrapper.class);

		try {
			String objectAsString = Prestashop17ClientUtil.convertToUTF8(xmlMapper.writeValueAsString(product));
			System.out.println(objectAsString);
		} catch(Exception e) {

		}
	}

	@Test
	public void deserializeCompleteProductCDataExample() throws IOException {
		XmlMapper xmlMapper = new XmlMapper();
		InputStream is = Prestashop17ClientDeserializeTest.class.getResourceAsStream("/example_responses/product_complete_production_with_cdata_1-7-3.xml");
		String fileAsString = convert(is);
//		fileAsString = fileAsString.replaceAll("<br />", "");
//		fileAsString = fileAsString.replaceAll("<br/>", "");
		ProductDescriptionsWrapper product = xmlMapper.readValue(fileAsString, ProductDescriptionsWrapper.class);

		try {
			String objectAsString = Prestashop17ClientUtil.convertToUTF8(xmlMapper.writeValueAsString(product));
			System.out.println(objectAsString);
		} catch(Exception e) {

		}
	}

	@Test
	public void deserializeTestList() throws IOException {
		XmlMapper xmlMapper = new XmlMapper();
		File inputFile = new File("src/test/resources/example_responses/testList.xml");
		TestList product = xmlMapper.readValue(inputFile, TestList.class);

		assertThat(product.getFriendlyUrls().getLanguages().size()).isEqualTo(2);
		assertThat(product.getFriendlyUrls().getLanguages().get(0).getId()).isEqualTo(1);
		assertThat(product.getFriendlyUrls().getLanguages().get(0).getValue().trim()).isEqualTo("first language rewrite link");

		assertThat(product.getNames().getLanguages().size()).isEqualTo(2);
		assertThat(product.getNames().getLanguages().get(1).getId()).isEqualTo(2);
		assertThat(product.getNames().getLanguages().get(1).getValue().trim()).isEqualTo("second language name");

		String stringValue = xmlMapper.writeValueAsString(product);
		System.out.println(stringValue);
		assertThat(stringValue.contains("<language id=\"")).isTrue();
		log.info("Product: {}", product);
	}

	private static String convert(InputStream is) {
		try {
			return StreamUtils.copyToString(is, Charset.forName("UTF-8"));
		} catch(Exception e) {
			throw new IllegalStateException(e);
		}
	}
}