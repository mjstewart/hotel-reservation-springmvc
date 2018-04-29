package com.demo.reservation;

import com.demo.domain.Extra;
import com.demo.domain.MealPlan;
import com.demo.domain.Reservation;
import com.demo.domain.RoomType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExtrasService {

    private ExtraRepository extraRepository;

    public ExtrasService(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    public List<Extra> getGeneralExtras(Extra.Type type) {
        return extraRepository.findAllByTypeAndCategory(type, Extra.Category.General);
    }

    public List<Extra> getFoodExtras(Extra.Type type) {
        return extraRepository.findAllByTypeAndCategory(type, Extra.Category.Food);
    }

    public List<Extra> getExtrasById(List<Long> ids) {
        List<Extra> target = new ArrayList<>();
        extraRepository.findAllById(ids).forEach(target::add);
        return target;
    }
}
