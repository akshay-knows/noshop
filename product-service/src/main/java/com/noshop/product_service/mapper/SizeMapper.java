package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateSizeRequest;
import com.noshop.product_service.dto.response.SizeResponse;
import com.noshop.product_service.entity.Size;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SizeMapper {

    Size toEntity(CreateSizeRequest request);

    SizeResponse toResponse(Size size);

}