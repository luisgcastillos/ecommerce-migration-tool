package org.smooth.systems.ec.magento19.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by David Monichi <david.monichi@smooth-systems.solutions> on 09.02.18.
 *
 * attribute_id ... 31 == name
 * attribute_id ... 33 == friendly-url
 * attribute_id ... 36 == sub title
 * attribute_id ... 47 == full url store dependent, storeId == 0
 */
@Entity
@Table(name="catalog_product_entity_varchar")
public class Magento19ProductVarchar extends Magento19Attributes {

	public static final Long NAME_ATTR_ID = 56L;
	public static final Long FRIENDLY_URL_ATTR_ID = 83L; // 82 without .html
	public static final Long META_TITLE_ATTR_ID = 67L;
	public static final Long META_DESCRIPTION_ATTR_ID = 69L;
	public static final Long MAIN_IMAGE_URL = 70L;
}