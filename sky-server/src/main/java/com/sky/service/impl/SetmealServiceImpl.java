package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入数据
        setmealMapper.insert(setmeal);
        //获取生成的套餐id
        Long SeatmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(SeatmealId);
        });
        //保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum=setmealPageQueryDTO.getPage();
        int pageSize=setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum,pageSize);
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }


    /**
     * 批量删除套餐
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前套餐是否正在起售，起售则无法删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus()== StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }

        }
        ids.forEach( setmealId->{
                //删除套餐表中的数据
                setmealMapper.deleteById(setmealId);
        //删除套餐表中的关联数据（操作中间表）
        setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }


    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //修改套餐表基本信息
        setmealMapper.update(setmeal);
        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        Long setmealId=setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish ->{
                setmealDish.setSetmealId(setmealId);
    });

        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);
    }


    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status==StatusConstant.ENABLE)
        {
            List<Dish> dishLish=dishMapper.getBySetmealId(id);
            if(dishLish!=null && dishLish.size()>0){
                dishLish.forEach(dish -> {
                    if(dish.getStatus()==StatusConstant.DISABLE)
                    {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal=Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);

    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list=setmealMapper.list(setmeal);
        return list;
    }

    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }


}
