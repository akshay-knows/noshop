package com.noshop.product_service.util;

import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.ProductVariant;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.enums.ImageSource;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Seeds repeatable catalog data for local development and load testing. */
@Component
@Order(10)
@RequiredArgsConstructor
public class ProductDataSeeder implements CommandLineRunner {

    private static final List<String> BRAND_NAMES = List.of(
            "FreshMart", "DailyBasket", "UrbanHarvest", "GreenLeaf", "PureChoice",
            "NatureNest", "QuickCart", "FarmDirect", "GoodGrain", "PrimeKitchen"
    );

    private static final List<String> CATEGORY_NAMES = List.of(
            "Dairy", "Beverages", "Staples", "Snacks", "Personal Care",
            "Household", "Fruits", "Vegetables", "Breakfast", "Bakery"
    );

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;

    @Value("${noshop.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${noshop.seed.product-count:1000}")
    private int productCount;

    /** Creates the configured demo catalog only when seeding is explicitly enabled. */
    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || productCount <= 0) {
            return;
        }

        List<Brand> brands = ensureBrands();
        List<Category> categories = ensureCategories();

        long existing = productRepository.count();
        if (existing >= productCount) {
            return;
        }

        int start = (int) existing + 1;
        List<Product> products = new ArrayList<>(productCount - start + 1);

        for (int number = start; number <= productCount; number++) {
            Brand brand = brands.get((number - 1) % brands.size());
            Category category = categories.get((number - 1) % categories.size());
            SubCategory subCategory = ensureSubCategory(category, (number - 1) % 3 + 1);

            Product product = Product.builder()
                    .name("Demo Product " + number)
                    .slug("demo-product-" + number)
                    .description("Seeded catalog product " + number + " for local development.")
                    .productType(category.getName())
                    .status(number % 20 == 0
                            ? ProductStatus.DISCONTINUED
                            : number % 10 == 0
                            ? ProductStatus.INACTIVE
                            : ProductStatus.ACTIVE)
                    .brand(brand)
                    .category(category)
                    .subCategory(subCategory)
                    .build();

            ProductVariant variant = ProductVariant.builder()
                    .sku("NOSHOP-SKU-" + number)
                    .packSize(BigDecimal.valueOf((number % 10) + 1L))
                    .unit("UNIT")
                    .price(BigDecimal.valueOf(50 + (number % 450)).setScale(2))
                    .status(ProductStatus.ACTIVE)
                    .product(product)
                    .build();

            ProductImage image = ProductImage.builder()
                    .imageUrl("https://placehold.co/600x600/png?text=NoShop-" + number)
                    .altText("Demo Product " + number)
                    .displayOrder(1)
                    .source(ImageSource.CATALOG)
                    .product(product)
                    .build();

            product.getVariants().add(variant);
            product.getImages().add(image);
            products.add(product);
        }

        productRepository.saveAll(products);
    }

    /** Ensures the reusable demo brands exist before products are created. */
    private List<Brand> ensureBrands() {
        List<Brand> brands = new ArrayList<>();

        for (String name : BRAND_NAMES) {
            Brand brand = brandRepository.findByName(name)
                    .orElseGet(() -> brandRepository.save(
                            Brand.builder()
                                    .name(name)
                                    .description("Seeded demo brand")
                                    .active(true)
                                    .build()
                    ));
            brands.add(brand);
        }

        return brands;
    }

    /** Ensures the reusable demo categories exist before products are created. */
    private List<Category> ensureCategories() {
        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < CATEGORY_NAMES.size(); i++) {
            String name = CATEGORY_NAMES.get(i);
            String slug = name.toLowerCase().replace(' ', '-');
            int displayOrder = i + 1;

            Category category = categoryRepository.findBySlug(slug)
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder()
                                    .name(name)
                                    .slug(slug)
                                    .displayOrder(displayOrder)
                                    .active(true)
                                    .build()
                    ));
            categories.add(category);
        }

        return categories;
    }

    /** Ensures one deterministic demo subcategory exists for the selected category. */
    private SubCategory ensureSubCategory(Category category, int suffix) {
        String slug = category.getSlug() + "-demo-" + suffix;

        return subCategoryRepository.findBySlug(slug)
                .orElseGet(() -> subCategoryRepository.save(
                        SubCategory.builder()
                                .name(category.getName() + " Demo " + suffix)
                                .slug(slug)
                                .displayOrder(suffix)
                                .active(true)
                                .category(category)
                                .build()
                ));
    }
}
