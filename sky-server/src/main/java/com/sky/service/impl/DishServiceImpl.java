package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j

public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Transactional
    public void savewithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入一条数据
        dishMapper.insert(dish);//DTO含有口味对象，所以重新定义一个新对象
        Long dishid = dish.getId();


        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishid);

            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }
    }


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */

    @Transactional
    public void deleteBatch(List<Long> ids) {

        //判断当前菜品是否能够删除----是否在起售中
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE)//菜品出于其手中
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);

            }
            //判断当前菜品是否被关联
            List<Long> setmealIds =setmealDishMapper.getSetmealIdsByDishIds(ids);
            if(setmealIds!=null && setmealIds.size()>0){
                //当前菜品被关联，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }




        }
//        //删除菜品表中得菜品数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//
//            //删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //根据菜品id集合删除菜品关联的套餐数据

        dishMapper.deleteByIds(ids);
        //根据菜品id集合删除关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }



    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据

        Dish dish=dishMapper.getById(id);

//        根据菜品id查询口味数据

        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);

        //将查询到的数据封装到VO
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }



    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息

        dishMapper.update(dish);
        //删除原有的口味信息

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //添加新的口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }


    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

    }
    /**
     * 条件查询
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList=dishMapper.list(dish);
        List<DishVO> dishVOList=new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO=new DishVO();
            BeanUtils.copyProperties(d,dishVO);
            //根据菜品id查询对应的口味
            List<DishFlavor> flavors=dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
