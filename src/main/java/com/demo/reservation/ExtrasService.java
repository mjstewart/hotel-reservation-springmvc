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

    public List<Extra> getExtras(RoomType roomType, Extra.Category category) {
        switch (roomType) {
            case Luxury:
            case Business:
                return extraRepository.findAllByTypeAndCategory(Extra.Type.Premium, category);
            case Balcony:
            case Economy:
                return extraRepository.findAllByTypeAndCategory(Extra.Type.Basic, category);
        }
        return new ArrayList<>();
    }

    public List<Extra> getExtrasById(List<Long> ids) {
        List<Extra> target = new ArrayList<>();
        extraRepository.findAllById(ids).forEach(target::add);
        return target;
    }
}
