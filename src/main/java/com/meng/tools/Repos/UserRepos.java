package com.meng.tools.Repos;

import com.meng.tools.entity.Myuser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description:
 * @author: xiapq
 * @date: 2019-06-15 16:25
 */
public interface UserRepos extends JpaRepository<Myuser, Long> {

    Myuser findUserByName(String name);
}
