select a.figi_title                                                   figi,
       a.strategy                                                     strategy,
       round(100 * a.total / (a.first_price * a.lots), 2)             profit_by_robot,
       round(100 * (a.last_price - a.first_price) / a.first_price, 2) profit_by_invest,
       a.offers                                                       offers,
       a.first_price                                                  first_price,
       a.last_price                                                   last_price
from (select o.figi_title,
             o.strategy,
             o.lots,
             count(o.*)                                                                                     offers,
             sum((o.sell_price - o.purchase_price) * o.lots - o.purchase_commission - o.sell_commission)    total,
             sum(o.purchase_commission + o.sell_commission)                                                 commission,
             (select c.closing_price from candle c where c.figi = o.figi order by c.date_time desc limit 1) last_price,
             (select oi.purchase_price from offer oi where oi.figi = o.figi order by oi.id limit 1)         first_price
      from offer o
      group by figi_title, figi, strategy, lots
      order by figi_title, total desc, strategy) a
order by figi_title, total desc, strategy