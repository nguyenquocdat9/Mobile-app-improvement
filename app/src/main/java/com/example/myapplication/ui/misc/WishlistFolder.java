package com.example.myapplication.ui.misc;

import java.util.ArrayList;
import java.util.List;

public class WishlistFolder {
    private String name;
    private List<Post> posts;

    public WishlistFolder(String name) {
        this.name = name;
        this.posts = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Post> getPosts() { return posts; }

    public void setPosts(List<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
    }

    public void addPost(Post post) { posts.add(post); }
    public void removePost(Post post) {
        posts.remove(post);
    }
}