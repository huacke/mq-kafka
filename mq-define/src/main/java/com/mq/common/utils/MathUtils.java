package com.mq.common.utils;

import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 */
public class MathUtils {

	private static final Double MONEY_RANGE = 0.01;

	/**
	 * 比较2个金额是否相等
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static Boolean equals(Double d1, Double d2) {
		Double result = Math.abs(d1 - d2);
		if (result < MONEY_RANGE) {
			return true;
		}else {
			return false;
		}
	}
	/**
	 * 判断整数是否大于零
	 *
	 * @param number
	 * @return
	 */
	public static boolean isIntThanZero(int number) {
		if (number > 0) {
			return true;
		}
		return false;
	}
	/**
	 * 判断整数是否相等
	 *
	 * @param d1 d2
	 * @return
	 */
	public static boolean equals(BigDecimal d1,BigDecimal d2) {
		if (d1.compareTo(d2)==0) { // -1小于，0等于，1大于
			return true;
		}
		return false;
	}
	/**
	 * 判断整数是否大于零
	 *
	 * @param number
	 * @return
	 */
	public static boolean isThanZero(BigDecimal number) {
		if (number.compareTo(BigDecimal.ZERO)==1) { // -1小于，0等于，1大于
			return true;
		}
		return false;
	}
	/**
	 * 判断整数是否小于零
	 *
	 * @param number
	 * @return
	 */
	public static boolean isLessZero(BigDecimal number) {
		if (number.compareTo(BigDecimal.ZERO) == -1) { // -1小于，0等于，1大于
			return true;
		}
		return false;
	}
	/**
	 * 判断BigDecimal是否为空或者0
	 * 
	 */
	public static boolean isEmpty (BigDecimal param) {
		if (param == null || param.compareTo(BigDecimal.ZERO) == 0) {
			return true;
		}
		return false;
	}
	/**
	 * 判断Integer是否为空或者0
	 *
	 */
	public static boolean isEmpty (Integer param) {
		if (param == null || param.equals(0)) {
			return true;
		}
		return false;
	}
	/**
	 * 将字符串转化为BigDecimal,空&null字符串转化为BigDecimal的0
	 * 
	 */
	public static BigDecimal convertStr2BigDecimal (String param) {
		if (param == null || "".equals(param)) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(param);
	}
	/**
	 * 去除小数点后多余的0
	 * */
	public static BigDecimal removeAmtLastZero(BigDecimal num) {
		String strNum = num.toString();
		if (strNum.indexOf('.') != -1) {
			String[] arr = strNum.split("\\.");
			String strDecimals = arr[1];
			List<String> list = new ArrayList<String>();
			boolean isCanAdd = false;
			for (int i = strDecimals.length() - 1; i > -1; i--) {
				String ss = String.valueOf(strDecimals.charAt(i));
				if (!"0".equals(ss)) {
					isCanAdd = true;// 从最后的字符开始算起，遇到第一个不是0的字符开始都是需要保留的字符
				}
				if (!"0".equals(ss) || isCanAdd) {
					list.add(ss);
				}
			}
			StringBuffer strZero = new StringBuffer();
			for (int i = list.size() - 1; i > -1; i--) {
				strZero.append(list.get(i));
			}
			strNum = String.format("%s.%s", arr[0], strZero.toString());
		}
		return new BigDecimal(strNum);
	}
	/**
	 * 根据消费金额折算成积分1:1
	 * 
	 */
	public static long retrievePointByAmount (BigDecimal amount) {
		long point = retrievePointByAmount(amount, BigDecimal.ONE);
		return point;
	}

	/**
	 * 根据消费金额,折算率,折算成积分
	 * 
	 */
	public static long retrievePointByAmount (BigDecimal amount, BigDecimal rate) {
		if (isEmpty(amount)) {
			return 0L;
		}
		BigDecimal temp = amount.multiply(rate);
		return temp.longValue();
	}

	/**
	 * 将金额格式化输出(进位)
	 * 19.256进位后为19.26
	 */
	public static BigDecimal formatAmount (BigDecimal amount) {
		if (isEmpty(amount)) {
			amount = BigDecimal.ZERO;
		}
		//保留2位小数，向上取整
		return amount.setScale(2, BigDecimal.ROUND_UP);
	}

	/**
	 * 将金额格式化输出(用来计算优惠，舍位)
	 * 19.256舍位后为19。25
	 */
	public static BigDecimal formatAmountDown (BigDecimal amount) {
		if (isEmpty(amount)) {
			amount = BigDecimal.ZERO;
		}
		//保留2位小数，向下取整
		return amount.setScale(2, BigDecimal.ROUND_DOWN);
	}

	/**
	 * 将金额格式化输出(用来计算优惠，舍位)
	 * 四舍五入
	 */
	public static BigDecimal formatAmountRound (BigDecimal amount) {
		if (isEmpty(amount)) {
			amount = BigDecimal.ZERO;
		}
		//保留2位小数，向下取整
		return amount.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 除法运算
	 */
	public static BigDecimal devide (BigDecimal devidend, BigDecimal divisor) {
		BigDecimal result = devidend.divide(divisor,10,BigDecimal.ROUND_HALF_UP);
		//保留4位小数，四舍五入
		return result;
	}

	public static boolean isBlank(Long id){
		if(null==id || "".equals(id) || 0==id){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isBlank(BigDecimal id){
		if(null==id || "".equals(id) || id.compareTo(BigDecimal.ZERO)== 0){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isBlank(Integer id){
		if(null==id || "".equals(id) || id.compareTo(0)== 0){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 判断是否是数字
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
	public static void main(String[] args) {

		BigDecimal d1 = new BigDecimal(0.01);

		System.out.println(new BigDecimal(0.01).setScale(4,BigDecimal.ROUND_HALF_UP).toPlainString());

		System.out.println(MathUtils.removeAmtLastZero(new BigDecimal(0.01)).toString());
		//stripTrailingZeros().toPlainString()
		System.out.println(d1.stripTrailingZeros().toPlainString());
	}

	/**
	 * 计算占比
	 * @param part,partList
	 * @return ratio
	 */
	public static BigDecimal calutApportionmentRatio(BigDecimal part, List<BigDecimal> partList){
		BigDecimal ratio=new BigDecimal("0");
		if(!ObjectUtils.isEmpty(part)&&!ObjectUtils.isEmpty(partList)&&partList.size()>=1){
			BigDecimal denominator = partList.stream()
					.filter(item-> item != null || !item.equals(BigDecimal.ZERO))
					.reduce(BigDecimal.ZERO,BigDecimal::add);
//			System.out.println(denominator);
			if(!denominator.equals(BigDecimal.ZERO)){
				ratio=part.divide(denominator,48,BigDecimal.ROUND_HALF_UP);
			}

		}
		return ratio;
	}
}