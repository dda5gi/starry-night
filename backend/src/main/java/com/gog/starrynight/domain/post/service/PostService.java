package com.gog.starrynight.domain.post.service;

import com.gog.starrynight.common.dto.AreaRange;
import com.gog.starrynight.common.dto.PagedResult;
import com.gog.starrynight.common.exception.ResourceForbiddenException;
import com.gog.starrynight.common.exception.ResourceNotFoundException;
import com.gog.starrynight.common.util.DataFileUtil;
import com.gog.starrynight.domain.achievement.entity.Achievement;
import com.gog.starrynight.domain.achievement.repository.AchievementRepository;
import com.gog.starrynight.domain.achievement_constellation.repository.AchievementConstellationRepository;
import com.gog.starrynight.domain.constellation.dto.ConstellationSimpleInfo;
import com.gog.starrynight.domain.constellation.entity.Constellation;
import com.gog.starrynight.domain.constellation.repository.ConstellationRepository;
import com.gog.starrynight.domain.constellation_history.entity.ConstellationHistory;
import com.gog.starrynight.domain.constellation_history.repository.ConstellationHistoryRepository;
import com.gog.starrynight.domain.datafile.entity.DataFile;
import com.gog.starrynight.domain.datafile.repository.DataFileRepository;
import com.gog.starrynight.domain.post.dto.*;
import com.gog.starrynight.domain.post.entity.Post;
import com.gog.starrynight.domain.post.repository.PostRepository;
import com.gog.starrynight.domain.post_image.dto.PostImageInfo;
import com.gog.starrynight.domain.post_image.entity.PostImage;
import com.gog.starrynight.domain.post_image.repository.PostImageRepository;
import com.gog.starrynight.domain.post_like.repository.PostLikeRepository;
import com.gog.starrynight.domain.user.dto.UserSimpleInfo;
import com.gog.starrynight.domain.user.entity.User;
import com.gog.starrynight.domain.user.repository.UserRepository;
import com.gog.starrynight.domain.user_achievement.entity.UserAchievement;
import com.gog.starrynight.domain.user_achievement.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ConstellationRepository constellationRepository;
    private final ConstellationHistoryRepository constellationHistoryRepository;
    private final PostImageRepository postImageRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementConstellationRepository achievementConstellationRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PostLikeRepository postLikeRepository;
    private final DataFileRepository dataFileRepository;
    private final DataFileUtil dataFileUtil;

    @Transactional
    public PostInfo createPost(PostCreateRequest dto, MultipartFile[] images, Long requesterId) throws IOException {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 회원입니다."));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .writer(requester)
                .build();

        postRepository.save(post);

        // 이미지가 존재한다면
        if (images != null) {
            addPostImages(post, images);
        }

        // 별자리를 기록했다면
        if (dto.getConstellations() != null) {
            addConstellationHistories(post, dto.getConstellations()); // 별자리 본 기록 추가
            checkAndGrantAchievement(requester); // 업적 달성 가능 여부 확인 후 업적 달성
        }

        return new PostInfo(post);
    }

    public PostDetailInfo getPostDetailInfo(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시물입니다."));

        return convertPostToPostDetailInfo(post, requesterId);
    }

    public PagedResult<PostDetailInfo> searchPost(PostSearchRequest dto, Long requesterId) {
        Sort.Direction direction = dto.getDirection().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, dto.getSort());
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        AreaRange areaRange;
        if (dto.getPointA() != null && dto.getPointB() != null && dto.getPointA().length == 2 && dto.getPointB().length == 2) {
            areaRange = calculateAreaRange(dto.getPointA(), dto.getPointB());
        } else {
            areaRange = null;
        }

        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 제목 검색 조건
            if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + dto.getTitle() + "%"));
            }

            // 내용 검색 조건
            if (dto.getContent() != null && !dto.getContent().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("content"), "%" + dto.getContent() + "%"));
            }

            // 위치 검색 조건
            if (areaRange != null) {
                predicates.add(criteriaBuilder.between(root.get("lat"), areaRange.getMinLat(), areaRange.getMaxLat()));
                predicates.add(criteriaBuilder.between(root.get("lng"), areaRange.getMinLng(), areaRange.getMaxLng()));
            }

            // 작성자 검색 조건
            if (dto.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("writer"), dto.getUserId()));
            }

            // 모든 검색 조건 AND 연산 결합
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Post> queryResult = postRepository.findAll(spec, pageable);

        Page<PostDetailInfo> processedResult = queryResult.map(post -> convertPostToPostDetailInfo(post, requesterId));

        return new PagedResult<>(processedResult);
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시물입니다."));

        if (!post.getWriter().getId().equals(requesterId)) {
            throw new ResourceForbiddenException("자신이 작성한 게시물만 삭제할 수 있습니다.");
        }

        List<PostImage> postImages = post.getPostImages();

        for (PostImage postImage : postImages) {
            DataFile dataFile = postImage.getDataFile();
            String filePath = dataFileUtil.getFullPath(dataFile.getStoredFileName());
            dataFileUtil.deleteFile(filePath);
        }

        List<ConstellationHistory> constellationHistories = post.getConstellationHistories();
        for (int i = constellationHistories.size() - 1; i >= 0; i--) {
            constellationHistories.get(i).setPost(null);
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostInfo updatePost(Long postId, PostUpdateRequest dto, MultipartFile[] addedImages, Long requesterId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시물입니다."));

        if (!post.getWriter().getId().equals(requesterId)) {
            throw new ResourceForbiddenException("자신의 게시물만 수정할 수 있습니다.");
        }

        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        if (dto.getLat() != null) {
            post.setLat(dto.getLat());
        }

        if (dto.getLng() != null) {
            post.setLng(dto.getLng());
        }

        if (dto.getDeletedImages() != null) {
            deletePostImages(dto.getDeletedImages());
        }

        if (dto.getDeletedConstellations() != null) {
            deleteConstellationHistories(postId, dto.getDeletedConstellations());
        }

        if (addedImages != null) {
            addPostImages(post, addedImages);
        }

        if (dto.getAddedConstellations() != null) {
            addConstellationHistories(post, dto.getAddedConstellations());
        }

        return new PostInfo(post);
    }


    @Transactional
    public void addPostImages(Post post, MultipartFile[] images) throws IOException {
        List<DataFile> dataFiles = new ArrayList<>();
        List<PostImage> postImages = new ArrayList<>();
        for (MultipartFile image : images) {
            DataFile dataFile = dataFileUtil.storeFile(image);

            PostImage postImage = PostImage.builder()
                    .dataFile(dataFile)
                    .post(post)
                    .build();

            dataFiles.add(dataFile);
            postImages.add(postImage);
        }

        dataFileRepository.saveAll(dataFiles);

        postImageRepository.saveAll(postImages);
        post.setPostImages(postImages);
    }

    @Transactional
    public void addConstellationHistories(Post post, Long[] constellations) {
        List<ConstellationHistory> constellationHistories = new ArrayList<>();
        for (Long constellationId : constellations) {
            Constellation constellation = constellationRepository.findById(constellationId)
                    .orElseThrow(() -> new ResourceNotFoundException("해당 별자리가 존재하지 않습니다."));

            ConstellationHistory constellationHistory = ConstellationHistory.builder()
                    .lat(post.getLat())
                    .lng(post.getLng())
                    .user(post.getWriter())
                    .post(post)
                    .constellation(constellation)
                    .build();

            constellationHistories.add(constellationHistory);
        }

        constellationHistoryRepository.saveAll(constellationHistories);
        post.setConstellationHistories(constellationHistories);
    }

    @Transactional
    public void deletePostImages(Long[] imageIds) {
        for (Long postImageId : imageIds) {
            PostImage postImage = postImageRepository.findById(postImageId)
                    .orElseThrow(() -> new ResourceNotFoundException("지우려고 하는 이미지가 존재하지 않습니다."));
            DataFile dataFile = postImage.getDataFile();
            String filePath = dataFileUtil.getFullPath(dataFile.getStoredFileName());
            dataFileUtil.deleteFile(filePath);
            postImageRepository.delete(postImage);
        }
    }

    @Transactional
    public void deleteConstellationHistories(Long postId, Long[] constellationHistoryIds) {
        for (Long constellationId : constellationHistoryIds) {
            ConstellationHistory constellationHistory = constellationHistoryRepository.findByConstellationIdAndPostId(constellationId, postId)
                    .orElseThrow(() -> new ResourceNotFoundException("지우려고 하는 별자리 태그가 존재하지 않습니다."));
            constellationHistoryRepository.delete(constellationHistory);
        }
    }

    public List<ObservationSpot> getObservationSpots(ObservationSpotSearchRequest dto) {
        AreaRange areaRange = null;

        if (dto.getPointA() != null && dto.getPointB() != null && dto.getPointA().length == 2 && dto.getPointB().length == 2) {
            areaRange = calculateAreaRange(dto.getPointA(), dto.getPointB());
        }

        List<Post> queryResult = null;

        if (areaRange != null) { // 영역을 받아왔다면 해당 영역에서 조회
            queryResult = postRepository.findAllByLatBetweenAndLngBetween(areaRange.getMinLat(), areaRange.getMaxLat(), areaRange.getMinLng(), areaRange.getMaxLng());
        } else { // 정한 영역이 없다면 전체 조회
            queryResult = postRepository.findAll();
        }

        return queryResult.stream()
                .map(ObservationSpot::new)
                .collect(Collectors.toList());
    }

    public PostDetailInfo convertPostToPostDetailInfo(Post post, Long requesterId) {
        UserSimpleInfo writer = new UserSimpleInfo(post.getWriter());
        boolean permission = (writer.getId().equals(requesterId));
        boolean postLikePossible = (requesterId != null);
        boolean postLiked = postLikeRepository.existsPostLikeByPostIdAndUserId(post.getId(), requesterId);

        List<PostImageInfo> images = post.getPostImages().stream()
                .map(PostImageInfo::new)
                .collect(Collectors.toList());

        List<ConstellationSimpleInfo> constellationTags = post.getConstellationHistories().stream()
                .map(constellationHistory -> {
                    Constellation constellation = constellationHistory.getConstellation();
                    return new ConstellationSimpleInfo(constellation);
                })
                .collect(Collectors.toList());

        int postLikeCount = postLikeRepository.getTotalPostLikeCountByPostId(post.getId());

        return new PostDetailInfo(post, writer, images, constellationTags, postLikeCount, postLikePossible, postLiked, permission);
    }

    @Transactional
    public void checkAndGrantAchievement(User requester) {
        List<Achievement> achievementList = achievementRepository.findAll();

        for (Achievement achievement : achievementList) {
            Optional<UserAchievement> userAchievement = userAchievementRepository.findByUserIdAndAchievementId(requester.getId(), achievement.getId());

            // 이미 달성한 업적이라면 패스
            if (userAchievement.isPresent()) {
                continue;
            }

            List<Long> constellationIds = achievementConstellationRepository.getConstellationIdsByAchievementId(achievement.getId());
            int totalConstellationCount = constellationIds.size();
            int completedConstellationsCount = constellationHistoryRepository.getCompletedConstellationCountByConstellationIdsAndUserId(constellationIds, requester.getId());

            if (completedConstellationsCount == totalConstellationCount) {
                UserAchievement newUserAchievement = UserAchievement.builder()
                        .user(requester)
                        .achievement(achievement)
                        .build();

                userAchievementRepository.save(newUserAchievement);
            }
        }
    }

    public AreaRange calculateAreaRange(double[] pointA, double[] pointB) {
        double minLat = 0;
        double maxLat = 0;
        double minLng = 0;
        double maxLng = 0;

        if (pointA[0] > pointB[0]) {
            minLat = pointB[0];
            maxLat = pointA[0];
        } else {
            minLat = pointA[0];
            maxLat = pointB[0];
        }

        if (pointA[1] > pointB[1]) {
            minLng = pointB[1];
            maxLng = pointA[1];
        } else {
            minLng = pointA[1];
            maxLng = pointB[1];
        }

        return new AreaRange(minLat, maxLat, minLng, maxLng);
    }


}
