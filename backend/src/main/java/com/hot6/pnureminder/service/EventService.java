package com.hot6.pnureminder.service;

import com.hot6.pnureminder.dto.EventDto;
import com.hot6.pnureminder.entity.AnnualPlan;
import com.hot6.pnureminder.entity.Event;
import com.hot6.pnureminder.entity.Member;
import com.hot6.pnureminder.exception.ResourceNotFoundException;
import com.hot6.pnureminder.exception.UnauthorizedException;
import com.hot6.pnureminder.mapperclass.AnnualPlanMapper;
import com.hot6.pnureminder.repository.AnnualPlanRepository;
import com.hot6.pnureminder.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final MemberService memberService;
    private final AnnualPlanRepository annualPlanRepository;
    private final AnnualPlanMapper annualPlanMapper;

    public EventDto createEvent(String username, EventDto eventDto) {
        Member member = memberService.findMemberByUsername(username);
        Event event = eventDto.toEntity();
        event.setMember(member);

        Event savedEvent = eventRepository.save(event);

        return EventDto.fromEntity(savedEvent);
    }

    public List<EventDto> getMonthEvents(String username, int month) {
        Member member = memberService.findMemberByUsername(username);
        Long memberId = member.getId();
        Integer state1 = member.getState();
        Integer state2 = 0;

        // 해당 Event가 어느 달에 존재하는지 확인
        YearMonth yearMonth = YearMonth.of(Year.now().getValue(), month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        // 이벤트 JPA
        List<Event> events = eventRepository.findAllEventsWithinMonthByMemberId(
                memberId, startOfMonth, endOfMonth
        );

        // 학사일정 JPA
        List<AnnualPlan> annualPlans = annualPlanRepository.findAllAnnualPlanWithinMonthByStateOrState(startOfMonth, endOfMonth, state1, state2);

        // DTO 변환
        List<EventDto> eventDtos = events.stream().map(EventDto::fromEntity).collect(Collectors.toList());

        // AnnualPlan을 EventDto로 변환
        List<EventDto> annualPlanDtos = annualPlans.stream()
                .map(annualPlan -> annualPlanMapper.annualPlanToEventDto(annualPlan))
                .toList();
        eventDtos.addAll(annualPlanDtos);

        return eventDtos;
    }
    public EventDto getEvent(String username, Long eventId){
        Long memberId = memberService.findMemberByUsername(username).getId();
        Event event = eventRepository.findByMemberIdAndEventId(memberId,eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for this id : " + eventId));
        return EventDto.fromEntity(event);
    }

    public EventDto updateEvent(String username, Long eventId, EventDto eventDtoToUpdate) {
        Member member = memberService.findMemberByUsername(username);
        Event existingEvent = eventRepository.findByMemberIdAndEventId(member.getId(), eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for this id : " + eventId));

        if (!existingEvent.getMember().getId().equals(member.getId())) {
            throw new UnauthorizedException("You are not allowed to update this event");
        }

        // Copy changes from DTO to Entity
        existingEvent.setTitle(eventDtoToUpdate.getTitle());
        existingEvent.setDescription(eventDtoToUpdate.getDescription());
        existingEvent.setLocation(eventDtoToUpdate.getLocation());
        existingEvent.setStartTime(eventDtoToUpdate.getStartTime());
        existingEvent.setEndTime(eventDtoToUpdate.getEndTime());
        existingEvent.setColor(eventDtoToUpdate.getColor());
        existingEvent.setAlarmTime(eventDtoToUpdate.getAlarmTime());

        // Save updated event
        Event updatedEvent = eventRepository.save(existingEvent);

        return EventDto.fromEntity(updatedEvent);
    }


    public void deleteEvent(String username, Long eventId) {
        Member member = memberService.findMemberByUsername(username);
        Event event = eventRepository.findByMemberIdAndEventId(member.getId(), eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for this id : " + eventId));

        if (!event.getMember().getId().equals(member.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this event");
        }

        eventRepository.delete(event);
    }




}
