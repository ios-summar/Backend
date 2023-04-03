package com.summar.summar.service;

import com.summar.summar.domain.Banner;
import com.summar.summar.dto.BannerResponseDto;
import com.summar.summar.results.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {
    private final BannerRepository bannerRepository;
    @Transactional(readOnly = true)
    public Stream<BannerResponseDto> findAllByBanner() {
        List<Banner> banners = bannerRepository.findAll();
        return banners.stream().map(BannerResponseDto::new);
    }
}
