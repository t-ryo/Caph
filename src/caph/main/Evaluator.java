package caph.main;

import java.util.*;


public class Evaluator extends CalcVisitor {

	HashMap<String, Object> record = new HashMap<String, Object>();
	Boolean record_ref;

	public Object eval(CalcTree node) {
		return node.accept(this);
	}

	@Override
	public Object visit(Source node) {
		record_ref = true;
		for (int i = 0; i < node.child.size(); i++) {
			Object buff = node.child.get(i).accept(this);
			if (buff != null)
				System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(Funcdecl node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		if (!record.containsKey(id))
			record.put(id, node);
		else {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(Arglist node) {
		return node.child;
	}

	@Override
	public Object visit(Arglist2 node) {
		return node.child;
	}

	@Override
	public Object visit(Returnlist node) {
		Object ret = null;

		for (int i = 0; i < node.child.size(); i++) {
			ret = node.child.get(i).accept(this);
			if (ret != null)
				break;
		}

		return ret;
	}

	@Override
	public Object visit(Return node) {
		if (Boolean.class.cast(node.child.get(1).accept(this)))
			return node.child.get(0).accept(this);
		else
			return null;
	}

	@Override
	public Object visit(OthwiseRet node) {
		return node.child.get(0).accept(this);
	}

	@Override
	public Object visit(Returncase node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left && right;
	}

	@Override
	public Object visit(Where node) {
		return node.child.get(0).accept(this);
	}

	@Override
	public Object visit(Declist node) {
		node.child.get(0).accept(this);
		node.child.get(1).accept(this);
		return null;
	}

	@Override
	public Object visit(FuncCall node) {
		HashMap<String, Object> buff = new HashMap<String, Object>(record);
		@SuppressWarnings("unchecked")
		List<CalcTree> arg2 = (List<CalcTree>) node.child.get(1).accept(this);
		Funcdecl cnode = Funcdecl.class.cast(node.child.get(0).accept(this));
		@SuppressWarnings("unchecked")
		List<CalcTree> arg = (List<CalcTree>) cnode.child.get(1).accept(this);
		Object ret;

		HashMap<String, Object> buff2 = new HashMap<String, Object>();

		// 新環境の構築
		record_ref = false;

		// 関数自身を環境に追加
		buff2.put(String.class.cast(cnode.child.get(0).accept(this)), cnode);

		// 引数を環境に追加
		for (int i = 0; i < arg.size(); i++) {
			String id = String.class.cast(arg.get(i).accept(this));
			if (!buff2.containsKey(id)){
				record_ref = true;
				buff2.put(id, arg2.get(i).accept(this));
				record_ref = false;
			}
			else {
				System.err.println("you can't do destructive assignment");
				System.exit(-1);
			}
		}
		
		record = buff2;

		// Where内の変数を追加
		if (cnode.child.size() == 4)
			cnode.child.get(3).accept(this);// Where

		record_ref = true;
		// 新環境の終了

		ret = cnode.child.get(2).accept(this);// Return

		record = buff;// 環境を元に戻す
		return ret;
	}

	@Override
	public Object visit(Add node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				//今までのAdd
				return Integer.class.cast(left) + Integer.class.cast(right);
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
				LinkedList<String> key = new LinkedList<String>();
				key.add("*");
				map.merge(key, Integer.class.cast(left), (x,y)->x+y);
				return map;
			} else if (right instanceof String) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> newRight = new LinkedList<String>();
				newRight.add(String.class.cast(right));
				map.put(newRight, 1);
				newRight.clear();
				newRight.add("*");
				map.put(newRight, Integer.class.cast(left));
				return map;
			} else {
				System.err.println("error");
				return null;
			}
		}else if (left instanceof HashMap<?, ?>) {
			HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) left;
			if (right instanceof Integer) {
				LinkedList<String> key = new LinkedList<String>();
				key.add("*");
				map.merge(key, Integer.class.cast(right), (x,y)->x+y);
				return map;
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map2 = (HashMap<LinkedList<String>, Integer>) right;
				for (LinkedList<String> key2 : map2.keySet()) {
					map.merge(key2, map2.get(key2), (x,y)->x+y);
				}
				return map;
			} else if (right instanceof String) {
				LinkedList<String> key = new LinkedList<String>();
				key.add(String.class.cast(right));
				map.merge(key, 1, (x,y)->x+y);
				return map;
			}else {
				System.err.println("error");
				return null;
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> newLeft = new LinkedList<String>();
				newLeft.add(String.class.cast(left));
				map.put(newLeft, 1);
				newLeft.clear();
				newLeft.add("*");
				map.put(newLeft, Integer.class.cast(right));
				return map;
			} else if (right instanceof HashMap<?, ?>) {
				Map<LinkedList<String>, Integer> map = (Map<LinkedList<String>, Integer>) right;
				LinkedList<String> key = new LinkedList<String>();
				key.add(String.class.cast(left));
				map.merge(key, 1, (x,y)->x+y);
				return map;
			} else if (right instanceof String) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> key = new LinkedList<String>();
				key.add(String.class.cast(left));
				map.put(key, 1);
				key.clear();
				key.add(String.class.cast(right));
				map.put(key, 1);
				return map;
			}else {
				System.err.println("error");
				return null;
			}
		} else {
			System.err.println("error");
			return null;
		}
	}

	@Override
	public Object visit(Mul node) {
		Object left = Object.class.cast(node.child.get(0).accept(this));
		Object right = Object.class.cast(node.child.get(1).accept(this));
		
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				//今までのMultiple
				return Integer.class.cast(left) * Integer.class.cast(right);
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
				map.forEach((key, value) -> map.merge(key,
						Integer.class.cast(left),
						(oldValue, newValue) -> oldValue * newValue));
				return map;
			} else if (right instanceof String) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> newRight = new LinkedList<String>();
				newRight.add(String.class.cast(right));
				map.put(newRight, Integer.class.cast(left));
				return map;
			} else {
				System.err.println("error");
				return null;
			}
		}else if (left instanceof HashMap<?, ?>) {
			HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) left;
			if (right instanceof Integer) {
				map.forEach((key, value) -> map.merge(key,
						Integer.class.cast(right),
						(oldValue, newValue) -> oldValue * newValue));
				return map;
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map2 = (HashMap<LinkedList<String>, Integer>) right;
				for (LinkedList<String> key : map.keySet()) {
					if (key.remove("*")) {
						for (LinkedList<String> key2 : map2.keySet()) {
							key.addAll(key2);
							map.merge(key, map2.get(key2), (x,y)->x*y);
						}
					} else {
						for (LinkedList<String> key2 : map2.keySet()) {
							if (key2.remove("*")) {
								map.merge(key, map2.get(key2), (x,y)->x*y);
							} else {
								key.addAll(key2);
								map.merge(key, map2.get(key2), (x,y)->x*y);
								Collections.sort(key);
							}
						}
					}
				}
				return map;
			} else if (right instanceof String) {
				String newRight = String.class.cast(right);
				for (LinkedList<String> key : map.keySet()) {
					if (key.remove("*")) {
						key.add(newRight);
					} else {
						key.add(newRight);
						Collections.sort(key);
					}
				}
				return map;
			}else {
				System.err.println("error");
				return null;
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> newLeft = new LinkedList<String>();
				newLeft.add(String.class.cast(left));
				map.put(newLeft, Integer.class.cast(right));
				return map;
			} else if (right instanceof HashMap<?, ?>) {
				HashMap<LinkedList<String>, Integer> map = (HashMap<LinkedList<String>, Integer>) right;
				String newLeft = String.class.cast(left);
				for (LinkedList<String> key : map.keySet()) {
					if (key.remove("*")) {
						key.add(newLeft);
					} else {
						key.add(newLeft);
						Collections.sort(key);
					}
				}
				return map;
			} else if (right instanceof String) {
				Map<LinkedList<String>, Integer> map = new HashMap<LinkedList<String>, Integer>();
				LinkedList<String> newKey = new LinkedList<String>();
				newKey.add(String.class.cast(left));
				newKey.add(String.class.cast(right));
				Collections.sort(newKey);
				map.put(newKey, 1);
				return map;
			}else {
				System.err.println("error");
				return null;
			}
		} else {
			System.err.println("error");
			return null;
		}
	}
	
	@Override
	public Object visit(Int node) {
		return node.val;
	}

	@Override
	public Object visit(Equals node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left == right) {
			return true;
		}
		return false;
	}

	public Object visit(NotEquals node) {
		Object left = node.child.get(0).accept(this);
		Object right = node.child.get(1).accept(this);
		if (left == right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(GreaterThan node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left > right) {
			return true;
		}
		return false;
	}

	@Override
	public Object visit(GreaterThanEquals node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(LessThan node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return true;
		}
		return false;
	}

	@Override
	public Object visit(LessThanEquals node) {
		Integer left = Integer.class.cast(node.child.get(0).accept(this));
		Integer right = Integer.class.cast(node.child.get(1).accept(this));
		if (left < right) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(And node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left && right;
	}

	@Override
	public Object visit(Or node) {
		Boolean left = Boolean.class.cast(node.child.get(0).accept(this));
		Boolean right = Boolean.class.cast(node.child.get(1).accept(this));
		return left || right;
	}

	@Override
	public Object visit(Vardecl node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		Object val = node.child.get(1).accept(this);
		if (!record.containsKey(id))
			record.put(id, val);
		else {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		return null;
	}

	@Override
	public Object visit(In node) {
		record_ref = false;
		String id = String.class.cast(node.child.get(0).accept(this));
		record_ref = true;
		if (record.containsKey(id)) {
			System.err.println("you can't do destructive assignment");
			System.exit(-1);
		}
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.err.println("please input \"" + id + "\"");
		String in = scan.next();
		switch (in) {
		case "true":
			record.put(id, true);
			break;
		case "false":
			record.put(id, false);
			break;
		default:
			record.put(id, Integer.parseInt(in));
			break;
		}
		return null;
	}

	@Override
	public Object visit(Out node) {
		System.out.println(node.child.get(0).accept(this));
		return null;
	}

	@Override
	public Object visit(Name node) {
		if (record.containsKey(node.str) && record_ref)
			return record.get(node.str);
		return node.str;
	}

	@Override
	public Object visit(Bool node) {
		return node.bool;
	}

	@Override
	public Object visit(Minus node) {
		return -1 * Integer.class.cast(node.child.get(0).accept(this));
	}

	@Override
	public Object visit(Not node) {
		return !Boolean.class.cast(node.child.get(0).accept(this));
	}

	@Override
	public Object visit(Monoral_bind node) {
		return node.child.get(0).accept(this);
	}

	@Override
	public Object visit(Parallel_bind node) {
		return node.child.get(0).accept(this);
	}

}
