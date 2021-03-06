package xcxxx.dao.Impl;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import xcxxx.dao.OrderDAO;
import xcxxx.entity.Book;
import xcxxx.entity.Order;
import xcxxx.entity.OrderItem;
import xcxxx.entity.User;
import xcxxx.utils.JdbcUtils;

import java.util.List;
import java.util.Set;

public class OrderDAOImpl implements OrderDAO{
    @Override
    public void add(Order order) {
        try {
            //1. 把order的基本信息保存到order表
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            String sql = "insert into orders(id,ordertime,price,state,user_id) values(?,?,?,?,?)";
            Object[] params = {order.getId(),order.getOrdertime(),order.getPrice(),order.isState(),order.getUser().getUserId()};
            runner.update(sql, params);
            //2. 把order中的订单项保存到orderitem表中
            Set<OrderItem> set = order.getOrderitems();
            for(OrderItem item: set){
                sql = "insert into orderitem(id,quantity,price,order_id,book_id) values(?,?,?,?,?)";
                params = new Object[]{item.getId(), item.getQuantity(),item.getPrice(),order.getId(),item.getBook().getId()};
                runner.update(sql,params);
            }
            sql = "insert into orderitem()";
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public Order find(String id) {
        try {
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            //1.找出订单的基本信息
            String sql = "select * from orders where id=?";
            Order order = (Order) runner.query(sql, new BeanHandler(Order.class), id);
            //2.找出订单中所有的订单项
            sql = "select * from orderitem where order_id=?";
            List<OrderItem> list = (List<OrderItem>)runner.query(sql, new BeanListHandler(OrderItem.class), id);
            for(OrderItem item : list){
                sql = "select book.* from orderitem,book where orderitem.id=? and orderitem.book_id=book.id";
                Book book = runner.query(sql, new BeanHandler<Book>(Book.class), item.getId());
                item.setBook(book);
            }
            //把找出的订单项放进order
            order.getOrderitems().addAll(list);
            //3.找出订单属于哪个用户
            sql = "select * from orders,user where orders.id=? and orders.user_id=user.userId";
            User user = (User) runner.query(sql, new BeanHandler(User.class), id);
            order.setUser(user);
            return order;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //后台获取所有订单
    @Override
    public List<Order> getAll(boolean state) {
        try {
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            String sql = "select * from orders where state=?";
            List<Order> list = (List<Order>) runner.query(sql, new BeanListHandler<Order>(Order.class), state);
            for(Order order : list){
                sql = "select user.* from orders,user where orders.id=? and orders.user_id=user.userId";
                User user = (User) runner.query(sql, new BeanHandler(User.class), order.getId());
                order.setUser(user);
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //这里只改变发货状态，实际中还可以改变购买数量等其他信息，可以再完善
    @Override
    public void update(Order order) {
        try {
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            String sql = "update orders set state = ? where id = ?";
            Object params[] = {order.isState(), order.getId()};
            runner.update(sql, params);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //前端页面中获取某个用户的所有订单
    @Override
    public List<Order> getAll(boolean state, String userid) {
        try {
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            String sql = "select * from orders where state=? and orders.user_id=?";
            Object params[] = {state, userid};
            List<Order> list = (List<Order>)runner.query(sql, new BeanListHandler<Order>(Order.class), params);
            sql = "select * from user where user.id=?";
            User user = runner.query(sql, new BeanHandler<User>(User.class), userid);
            for(Order order : list){
                order.setUser(user);
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Order> getAllOrder(String userid) {
        try {
            QueryRunner runner = new QueryRunner(JdbcUtils.getDataSource());
            String sql = "select * from orders where user_id=?";
            List<Order> list = runner.query(sql, new BeanListHandler<Order>(Order.class), userid);
            sql = "select * from user where userid=?";
            User user = (User)runner.query(sql, new BeanHandler(User.class), userid);
            for(Order order : list){
                order.setUser(user);
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
