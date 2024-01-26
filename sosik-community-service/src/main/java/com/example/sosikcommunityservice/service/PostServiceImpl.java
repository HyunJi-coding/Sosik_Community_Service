package com.example.sosikcommunityservice.service;

import com.example.sosikcommunityservice.dto.request.GetPostSliceCondition;
import com.example.sosikcommunityservice.dto.request.RequestCreatePost;
import com.example.sosikcommunityservice.dto.request.RequestUpdatePost;
import com.example.sosikcommunityservice.dto.response.ResponseGetComment;
import com.example.sosikcommunityservice.dto.response.ResponseGetPost;
import com.example.sosikcommunityservice.dto.response.ResponseGetPostList;
import com.example.sosikcommunityservice.exception.ApplicationException;
import com.example.sosikcommunityservice.exception.ErrorCode;
import com.example.sosikcommunityservice.model.entity.PostEntity;
import com.example.sosikcommunityservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public void createPost(Long memberId, RequestCreatePost postDTO) {
        PostEntity postentity = PostEntity.builder()
                .memberId(memberId)
                .title(postDTO.title())
                .content(postDTO.content())
                .hits(0)
                .build();
        postRepository.save(postentity);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ResponseGetPostList> getPostListSlice(GetPostSliceCondition condition) {
        Pageable pageable = PageRequest.of(condition.getPage(), condition.getSize());
        Slice<PostEntity> postSlice = postRepository.findAllByOrderByIdDesc(pageable);

        List<ResponseGetPostList> responseGetPostList = postSlice.getContent().stream()
                .map(ResponseGetPostList::responseGetPostList)
                .collect(Collectors.toList());

        return new SliceImpl<>(responseGetPostList, pageable, postSlice.hasNext());
    }

    @Override
    public ResponseGetPost getPost(Long postId) {
        Optional<PostEntity> postEntityOptional = postRepository.findById(postId);
        if (postEntityOptional.isEmpty()){
            return null;
        }

        PostEntity postEntity = postEntityOptional.get();
        return PostEntity.responseGetPost(postEntity);
    }

    @Override
    public void updatePost(Long postId, RequestUpdatePost updatePost) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> {
            return new ApplicationException(ErrorCode.UNAUTHORIZED_UPDATE);
        });
        postEntity.updatePost(updatePost);
    }

    @Override
    public void deletePost(Long postId) {
        if (postRepository.findById(postId) == null) {
            throw new ApplicationException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.deleteById(postId);
    }


}
