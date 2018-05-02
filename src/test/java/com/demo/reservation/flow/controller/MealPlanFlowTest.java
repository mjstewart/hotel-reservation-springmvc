package com.demo.reservation.flow.controller;

import com.demo.TimeProvider;
import com.demo.domain.*;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.flow.helpers.FlowMatchers;
import com.demo.reservation.flow.helpers.FlowStages;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class MealPlanFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // Flow step 4 - meal plans

    /**
     * Redirects to general extras form when in the meal plans form. Note the flash attributes
     * used are so the Model is not lost between redirects.
     */
    @Test
    public void fromMealPlansBackToGeneralExtras() throws Exception {
        ReservationFlow reservationFlow = FlowStages.extrasCompletedFlow();

        mockMvc.perform(post("/reservation/meals")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Meals))
                .andExpect(FlowMatchers.flashHasIncompleteFlowStep(ReservationFlow.Step.Meals))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * Assert that the Model contains the expected objects required for template generation.
     * Since creating meal plans is quite complex we need
     * <ul>
     * <li>foodExtras: Each guest will be able to select an extra which will be added to
     * their {@code MealPlan}
     * </li>
     * <li>dietaryRequirements: Each guest will be able to select a dietary requirement which
     * will be added to their {@code MealPlan}
     * </li>
     * <li>Sanity check to ensure each guest has a meal plan created ready to be binded to the thymeleaf view.</li>
     * <li>reservationFlow: The reservationFlow must still be kept in the Model to continue to stay in the Session.</li>
     * </ul>
     * <p>
     */
    @Test
    public void getMeals_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = FlowStages.extrasCompletedFlow();

        // A part of a meal plan, guests can choose 3 main daily meals
        List<Extra> foodExtras = List.of(
                new Extra("breakfast", BigDecimal.valueOf(3.20), Extra.Type.Basic, Extra.Category.Food),
                new Extra("lunch", BigDecimal.valueOf(5.40), Extra.Type.Basic, Extra.Category.Food),
                new Extra("dinner", BigDecimal.valueOf(8.90), Extra.Type.Basic, Extra.Category.Food)
        );

        // Make sure createMealPlans is called.
        Reservation reservationSpy = spy(reservationFlow.getReservation());
        reservationFlow.setReservation(reservationSpy);

        // So we can verify the correct call to get the food extras occurs.
        when(extraRepository.findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food)))
                .thenReturn(foodExtras);

        ResultMatcher expectedMealPlansCreated = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("mealPlans",
                                Matchers.hasSize(reservationFlow.getReservation().getGuests().size()))));

        mockMvc.perform(get("/reservation/meals")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/meals"))
                .andExpect(model().attribute("foodExtras", Matchers.equalTo(foodExtras)))
                .andExpect(model().attribute("dietaryRequirements", Matchers.equalTo(DietaryRequirement.values())))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(expectedMealPlansCreated)
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Meals))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Meals));

        verify(extraRepository, times(1))
                .findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food));
        verifyNoMoreInteractions(extraRepository);

        verify(reservationSpy, times(1)).createMealPlans();
    }

    /**
     * When the meal plans are submitted, check the flow step is completed for Meals and
     * we redirect to the next review view. flash attributes are needed so the Model state is not lost
     * across the redirect. Since there are no errors, the Model doesn't need to contain the foodExtras or
     * dietaryRequirements since we are redirecting to a new view.
     *
     * <p>We cant test for checking the meal plans have been updated within the reservation since all of that
     * happens using spring binding infrastructure so would need to make an integration test for that using
     * {@literal @SpringBootTest}.</p>
     */
    @Test
    public void postMealPlans_ValidDietaryRequirements() throws Exception {
        ReservationFlow reservationFlow = FlowStages.extrasCompletedFlow();

        Room room = FlowStages.createRoom();
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.addGuest(guest);
        reservation.createMealPlans();

        // The meal plan for the only guest
        MealPlan mealPlan = reservation.getMealPlans().get(0);
        mealPlan.setFoodExtras(List.of(new Extra("breakfast", BigDecimal.valueOf(3.4), Extra.Type.Basic, Extra.Category.Food)));

        // valid diets
        mealPlan.setDietaryRequirements(List.of(DietaryRequirement.Vegan, DietaryRequirement.GlutenIntolerant));

        mockMvc.perform(post("/reservation/meals")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/review"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(flash().attribute("foodExtras", Matchers.nullValue()))
                .andExpect(flash().attribute("dietaryRequirements", Matchers.nullValue()))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Meals))
                .andExpect(FlowMatchers.flashHasCompletedFlowStep(ReservationFlow.Step.Meals));
    }

    /**
     * <p>Should get validation error when a guest is vegan and vegetarian at the same time. Can only be
     * one or the other. Since there are errors, the same Model created when getting the meal plan form
     * needs to exist otherwise the food extras and dietary requirement checkboxes wont re appear.</p>
     *
     * <p>We cant test for checking the meal plans have been updated within the reservation since all of that
     * happens using spring binding infrastructure so would need to make an integration test for that using
     * {@literal @SpringBootTest}.</p>
     */
    @Test
    public void postMealPlans_InvalidDietaryRequirements() throws Exception {
        ReservationFlow reservationFlow = FlowStages.extrasCompletedFlow();
        Reservation reservation = reservationFlow.getReservation();

        Room room = FlowStages.createRoom();
        // Allow 2 guest to be added
        room.setBeds(2);
        Guest guestA = new Guest("john", "smith", false);
        Guest guestB = new Guest("nicole", "clarke", false);

        reservation.setRoom(room);
        reservation.addGuest(guestA);
        reservation.addGuest(guestB);
        reservation.createMealPlans();

        // Make both guests have invalid dietary requirements.
        MealPlan mealPlanA = reservation.getMealPlans().get(0);
        MealPlan mealPlanB = reservation.getMealPlans().get(1);
        mealPlanA.setFoodExtras(List.of(new Extra("breakfast", BigDecimal.valueOf(3.4), Extra.Type.Basic, Extra.Category.Food)));
        mealPlanB.setFoodExtras(List.of(new Extra("breakfast", BigDecimal.valueOf(3.4), Extra.Type.Basic, Extra.Category.Food)));

        // invalid diets
        mealPlanA.setDietaryRequirements(List.of(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian, DietaryRequirement.GlutenIntolerant));
        mealPlanB.setDietaryRequirements(List.of(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian, DietaryRequirement.GlutenIntolerant));

        // Recreate the food extras
        List<Extra> foodExtras = List.of(
                new Extra("breakfast", BigDecimal.valueOf(3.20), Extra.Type.Basic, Extra.Category.Food),
                new Extra("lunch", BigDecimal.valueOf(5.40), Extra.Type.Basic, Extra.Category.Food),
                new Extra("dinner", BigDecimal.valueOf(8.90), Extra.Type.Basic, Extra.Category.Food)
        );

        // So we can verify the correct call to get the food extras occurs.
        when(extraRepository.findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food)))
                .thenReturn(foodExtras);

        mockMvc.perform(post("/reservation/meals")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/meals"))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.mealPlans[0].dietaryRequirements", "VeganMismatch"))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.mealPlans[1].dietaryRequirements", "VeganMismatch"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attribute("foodExtras", Matchers.equalTo(foodExtras)))
                .andExpect(model().attribute("dietaryRequirements", Matchers.equalTo(DietaryRequirement.values())))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Meals))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Meals));

        verify(extraRepository, times(1))
                .findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food));
        verifyNoMoreInteractions(extraRepository);
    }
}
