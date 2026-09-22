package com.noshop.product_service.util;

import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.ProductVariant;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.enums.CatalogAudience;
import com.noshop.product_service.enums.ImageSource;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.enums.Unit;
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

@Component
@Order(10)
@RequiredArgsConstructor
public class ProductDataSeeder implements CommandLineRunner {

    private static final List<String> BRAND_NAMES = List.of(
            "Amul",
            "Britannia",
            "Tata",
            "Aashirvaad",
            "Fortune",
            "Parle",
            "Coca-Cola",
            "Nestle",
            "Mother Dairy",
            "India Gate",
            "Haldirams",
            "Kelloggs",
            "FreshMart"
    );

    private static final List<String> CATEGORY_NAMES = List.of(
            "Dairy",
            "Staples",
            "Beverages",
            "Snacks",
            "Bakery",
            "Breakfast",
            "Fruits & Vegetables",
            "Household"
    );

    private static final List<SeedProduct> CURATED_PRODUCTS = List.of(
            new SeedProduct("Amul Taaza Milk 1L", "amul-taaza-milk-1l", "Amul",
                    "Dairy", "Milk", CatalogAudience.BOTH, 1, Unit.LITER, "68"),
            new SeedProduct("Amul Butter 500g", "amul-butter-500g", "Amul",
                    "Dairy", "Butter", CatalogAudience.BOTH, 500, Unit.GRAM, "285"),
            new SeedProduct("Mother Dairy Curd 400g", "mother-dairy-curd-400g", "Mother Dairy",
                    "Dairy", "Curd", CatalogAudience.B2C, 400, Unit.GRAM, "45"),
            new SeedProduct("Amul Cheese Slices 200g", "amul-cheese-slices-200g", "Amul",
                    "Dairy", "Cheese", CatalogAudience.BOTH, 200, Unit.GRAM, "145"),
            new SeedProduct("Amul Paneer 200g", "amul-paneer-200g", "Amul",
                    "Dairy", "Paneer", CatalogAudience.B2B, 200, Unit.GRAM, "95"),

            new SeedProduct("Aashirvaad Whole Wheat Atta 5kg", "aashirvaad-atta-5kg", "Aashirvaad",
                    "Staples", "Flour", CatalogAudience.BOTH, 5, Unit.KILOGRAM, "280"),
            new SeedProduct("India Gate Basmati Rice 5kg", "india-gate-basmati-rice-5kg", "India Gate",
                    "Staples", "Rice", CatalogAudience.BOTH, 5, Unit.KILOGRAM, "650"),
            new SeedProduct("Tata Salt 1kg", "tata-salt-1kg", "Tata",
                    "Staples", "Salt", CatalogAudience.B2C, 1, Unit.KILOGRAM, "28"),
            new SeedProduct("Fortune Sunflower Oil 5L", "fortune-sunflower-oil-5l", "Fortune",
                    "Staples", "Cooking Oil", CatalogAudience.B2B, 5, Unit.LITER, "720"),
            new SeedProduct("Tata Sampann Moong Dal 1kg", "tata-sampann-moong-dal-1kg", "Tata",
                    "Staples", "Pulses", CatalogAudience.BOTH, 1, Unit.KILOGRAM, "155"),

            new SeedProduct("Tata Tea Gold 500g", "tata-tea-gold-500g", "Tata",
                    "Beverages", "Tea", CatalogAudience.BOTH, 500, Unit.GRAM, "245"),
            new SeedProduct("Coca-Cola 2L", "coca-cola-2l", "Coca-Cola",
                    "Beverages", "Soft Drinks", CatalogAudience.B2C, 2, Unit.LITER, "110"),
            new SeedProduct("Nescafe Classic 200g", "nescafe-classic-200g", "Nestle",
                    "Beverages", "Coffee", CatalogAudience.B2B, 200, Unit.GRAM, "420"),

            new SeedProduct("Parle-G Biscuits 800g", "parle-g-biscuits-800g", "Parle",
                    "Snacks", "Biscuits", CatalogAudience.B2C, 800, Unit.GRAM, "75"),
            new SeedProduct("Haldirams Aloo Bhujia 1kg", "haldirams-aloo-bhujia-1kg", "Haldirams",
                    "Snacks", "Namkeen", CatalogAudience.B2B, 1, Unit.KILOGRAM, "245"),

            new SeedProduct("Britannia Bread 400g", "britannia-bread-400g", "Britannia",
                    "Bakery", "Bread", CatalogAudience.B2C, 400, Unit.GRAM, "45"),
            new SeedProduct("Britannia Fruit Cake 300g", "britannia-fruit-cake-300g", "Britannia",
                    "Bakery", "Cakes", CatalogAudience.B2C, 300, Unit.GRAM, "120"),

            new SeedProduct("Kelloggs Corn Flakes 475g", "kelloggs-corn-flakes-475g", "Kelloggs",
                    "Breakfast", "Cereals", CatalogAudience.BOTH, 475, Unit.GRAM, "310"),
            new SeedProduct("Aashirvaad Oats 1kg", "aashirvaad-oats-1kg", "Aashirvaad",
                    "Breakfast", "Oats", CatalogAudience.B2B, 1, Unit.KILOGRAM, "170"),

            new SeedProduct("Fresh Apples 1kg", "fresh-apples-1kg", "FreshMart",
                    "Fruits & Vegetables", "Fresh Fruits", CatalogAudience.B2C, 1, Unit.KILOGRAM, "180"),
            new SeedProduct("Fresh Onions 1kg", "fresh-onions-1kg", "FreshMart",
                    "Fruits & Vegetables", "Fresh Vegetables", CatalogAudience.BOTH, 1, Unit.KILOGRAM, "42"),
            new SeedProduct("Fresh Tomatoes 1kg", "fresh-tomatoes-1kg", "FreshMart",
                    "Fruits & Vegetables", "Fresh Vegetables", CatalogAudience.BOTH, 1, Unit.KILOGRAM, "38"),

            new SeedProduct("Vim Dishwash 500ml", "vim-dishwash-500ml", "Tata",
                    "Household", "Cleaning", CatalogAudience.B2C, 500, Unit.MILLILITER, "110"),
            new SeedProduct("Tide Detergent 2kg", "tide-detergent-2kg", "Tata",
                    "Household", "Laundry", CatalogAudience.B2B, 2, Unit.KILOGRAM, "260")
    );

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;

    @Value("\${noshop.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("\${noshop.seed.product-count:30}")
    private int productCount;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || productCount <= 0) {
            return;
        }

        List<Brand> brands = ensureBrands();
        List<Category> categories = ensureCategories();

        ensureCuratedProducts(brands, categories);

        long existing = productRepository.count();
        if (existing >= productCount) {
            return;
        }

        int start = (int) existing + 1;
        List<Product> products = new ArrayList<>();

        for (int number = start; number <= productCount; number++) {
            Brand brand = brands.get((number - 1) % brands.size());
            Category category = categories.get((number - 1) % categories.size());
            SubCategory subCategory =
                    ensureSubCategory(category, "Demo " + ((number - 1) % 3 + 1));

            CatalogAudience audience =
                    switch (number % 3) {
                        case 0 -> CatalogAudience.B2B;
                        case 1 -> CatalogAudience.B2C;
                        default -> CatalogAudience.BOTH;
                    };

            Product product = Product.builder()
                    .name("Demo Grocery Product " + number)
                    .slug("demo-grocery-product-" + number)
                    .description("Seeded grocery catalog product " + number + " for local development.")
                    .productType(category.getName())
                    .status(number % 20 == 0
                            ? ProductStatus.DISCONTINUED
                            : number % 10 == 0
                            ? ProductStatus.INACTIVE
                            : ProductStatus.ACTIVE)
                    .audience(audience)
                    .brand(brand)
                    .category(category)
                    .subCategory(subCategory)
                    .build();

            ProductVariant variant = ProductVariant.builder()
                    .sku("NOSHOP-SKU-" + number)
                    .packSize(BigDecimal.valueOf((number % 10) + 1L))
                    .unit(Unit.values()[number % Unit.values().length])
                    .price(BigDecimal.valueOf(50 + (number % 450)).setScale(2))
                    .status(ProductStatus.ACTIVE)
                    .product(product)
                    .build();

            ProductImage image = ProductImage.builder()
                    .imageUrl("https://placehold.co/600x600/png?text=NoShop-" + number)
                    .altText("Demo Grocery Product " + number)
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

    private void ensureCuratedProducts(
            List<Brand> brands,
            List<Category> categories) {

        for (SeedProduct seed : CURATED_PRODUCTS) {
            if (productRepository.existsBySlug(seed.slug())) {
                continue;
            }

            Brand brand = findBrand(brands, seed.brandName());
            Category category = findCategory(categories, seed.categoryName());
            if (brand == null || category == null) {
                continue;
            }

            SubCategory subCategory =
                    ensureSubCategory(category, seed.subCategoryName());

            Product product = Product.builder()
                    .name(seed.name())
                    .slug(seed.slug())
                    .description("Seeded grocery product for B2B/B2C catalog testing.")
                    .productType(seed.subCategoryName())
                    .status(ProductStatus.ACTIVE)
                    .audience(seed.audience())
                    .brand(brand)
                    .category(category)
                    .subCategory(subCategory)
                    .build();

            ProductVariant variant = ProductVariant.builder()
                    .sku("GROCERY-" + seed.slug().toUpperCase().replace('-', '_'))
                    .packSize(BigDecimal.valueOf(seed.packSize()))
                    .unit(seed.unit())
                    .price(new BigDecimal(seed.price()))
                    .status(ProductStatus.ACTIVE)
                    .product(product)
                    .build();

            ProductImage image = ProductImage.builder()
                    .imageUrl("https://placehold.co/600x600/png?text="
                            + seed.name().replace(' ', '+'))
                    .altText(seed.name())
                    .displayOrder(1)
                    .source(ImageSource.CATALOG)
                    .product(product)
                    .build();

            product.getVariants().add(variant);
            product.getImages().add(image);
            productRepository.save(product);
        }
    }

    private List<Brand> ensureBrands() {
        List<Brand> brands = new ArrayList<>();

        for (String name : BRAND_NAMES) {
            Brand brand = brandRepository.findByName(name)
                    .orElseGet(() -> brandRepository.save(
                            Brand.builder()
                                    .name(name)
                                    .description("Seeded grocery brand")
                                    .active(true)
                                    .build()
                    ));
            brands.add(brand);
        }

        return brands;
    }

    private List<Category> ensureCategories() {
        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < CATEGORY_NAMES.size(); i++) {
            String name = CATEGORY_NAMES.get(i);
            String slug = toSlug(name);
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

    private SubCategory ensureSubCategory(Category category, String name) {
        String slug = category.getSlug() + "-" + toSlug(name);

        return subCategoryRepository.findBySlug(slug)
                .orElseGet(() -> subCategoryRepository.save(
                        SubCategory.builder()
                                .name(name)
                                .slug(slug)
                                .displayOrder(Math.abs(name.hashCode()) % 1000 + 1)
                                .active(true)
                                .category(category)
                                .build()
                ));
    }

    private Brand findBrand(List<Brand> brands, String name) {
        return brands.stream()
                .filter(brand -> brand.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private Category findCategory(List<Category> categories, String name) {
        return categories.stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private String toSlug(String value) {
        return value.toLowerCase()
                .replace("&", "and")
                .replace(' ', '-');
    }

    private record SeedProduct(
            String name,
            String slug,
            String brandName,
            String categoryName,
            String subCategoryName,
            CatalogAudience audience,
            long packSize,
            Unit unit,
            String price
    ) {
    }
}
