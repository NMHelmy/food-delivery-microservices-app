package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.exception.ForbiddenOperationException;
import com.fooddelivery.restaurantservice.models.MenuItem;
import com.fooddelivery.restaurantservice.models.Restaurant;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.exception.UnauthorizedException;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId, Boolean availableOnly) {
        List<MenuItem> items;
        if (availableOnly != null && availableOnly) {
            items = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
        } else {
            items = menuItemRepository.findByRestaurantId(restaurantId);
        }
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category, Boolean availableOnly) {
        List<MenuItem> items;
        if (availableOnly != null && availableOnly) {
            items = menuItemRepository.findByRestaurantIdAndCategoryAndIsAvailableTrue(restaurantId, category);
        } else {
            items = menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category);
        }
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long restaurantId, Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (!menuItem.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Menu item not found");
        }

        return mapToResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request, String ownerIdStr) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        Long ownerId = Long.parseLong(ownerIdStr);

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new ForbiddenOperationException("You are not authorized to add menu items to this restaurant");
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurantId(restaurantId);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        MenuItem saved = menuItemRepository.save(menuItem);
        return mapToResponse(saved);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long restaurantId, Long id, MenuItemRequest request, String ownerIdStr) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (!menuItem.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + id);
        }

        Restaurant restaurant = restaurantRepository.findById(menuItem.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Long ownerId = Long.parseLong(ownerIdStr);

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new ForbiddenOperationException("You are not authorized to update this menu item");
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }

        MenuItem updated = menuItemRepository.save(menuItem);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMenuItem(Long restaurantId, Long id, String ownerIdStr) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (!menuItem.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + id);
        }

        Restaurant restaurant = restaurantRepository.findById(menuItem.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Long ownerId = Long.parseLong(ownerIdStr);
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new ForbiddenOperationException("You are not authorized to delete this menu item");
        }

        menuItemRepository.delete(menuItem);
    }

    @Transactional
    public MenuItemResponse toggleAvailability(Long restaurantId, Long id, String ownerIdStr) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        if (!menuItem.getRestaurantId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + id);
        }

        Restaurant restaurant = restaurantRepository.findById(menuItem.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Long ownerId = Long.parseLong(ownerIdStr);
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new ForbiddenOperationException("You are not authorized to update this menu item");
        }

        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updated = menuItemRepository.save(menuItem);
        return mapToResponse(updated);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getRestaurantId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getIsAvailable(),
                menuItem.getImageUrl(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }
}