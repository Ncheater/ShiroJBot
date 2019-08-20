package com.kuuhaku.controller;

import com.kuuhaku.model.*;
import com.kuuhaku.utils.ExceedEnums;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.LogLevel;
import net.dv8tion.jda.core.entities.User;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

public class MySQL {

    private static EntityManagerFactory emf;

    private static EntityManager getEntityManager() {
        Map<String, String> props = new HashMap<>();
        props.put("javax.persistence.jdbc.user", System.getenv("DB_LOGIN"));
        props.put("javax.persistence.jdbc.password", System.getenv("DB_PASS"));

        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("shiro_remote", props);
            Helper.log(MySQL.class, LogLevel.INFO, "✅ | Ligação à base de dados MySQL estabelecida.");
        }

        emf.getCache().evictAll();

        return emf.createEntityManager();
    }

    public static void dumpData(DataDump data) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        data.getCaDump().forEach(em::merge);
        data.getGcDump().forEach(em::merge);

        for (int i = 0; i < data.getmDump().size(); i++) {
            em.merge(data.getmDump().get(i));
            if (i % 20 == 0) {
                em.flush();
                em.clear();
            }
            if (i % 1000 == 0) {
                em.getTransaction().commit();
                em.clear();
                em.getTransaction().begin();
            }
        }

        em.getTransaction().commit();
    }

    @SuppressWarnings("unchecked")
    public static DataDump getData() {
        EntityManager em = getEntityManager();

        Query ca = em.createQuery("SELECT c FROM CustomAnswers c", CustomAnswers.class);
        Query m = em.createQuery("SELECT m FROM Member m", Member.class);
        Query gc = em.createQuery("SELECT g FROM guildConfig g", guildConfig.class);

        return new DataDump(ca.getResultList(), m.getResultList(), gc.getResultList());
    }

    @SuppressWarnings("unchecked")
    public static List<Member> getMembers() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT m FROM Member m", Member.class);

        return q.getResultList();
    }

    public static void sendBeybladeToDB(Beyblade bb) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        em.merge(bb);
        em.getTransaction().commit();
    }

    public static Beyblade getBeybladeById(String id) {
        EntityManager em = getEntityManager();

        Beyblade bb;

        try {
            Query b = em.createQuery("SELECT b FROM Beyblade b WHERE id = ?1", Beyblade.class);
            b.setParameter(1, id);
            bb = (Beyblade) b.getSingleResult();

            return bb;
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Beyblade> getBeybladeList() {
        EntityManager em = getEntityManager();

        try {
            Query b = em.createQuery("SELECT b FROM Beyblade b", Beyblade.class);

            return (List<Beyblade>) b.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static Beyblade getChampionBeyblade() {
        List<Beyblade> rank = MySQL.getBeybladeList();
        assert rank != null;
        rank.sort(Comparator.comparing(Beyblade::getKDA));
        Collections.reverse(rank);
        return rank.get(0);
    }

    public static void permaBlock(PermaBlock p) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        em.merge(p);
        em.getTransaction().commit();

        em.close();
    }

    @SuppressWarnings("unchecked")
    public static List<String> blockedList() {
        EntityManager em = getEntityManager();

        try {
            Query q = em.createQuery("SELECT p.id FROM PermaBlock p", String.class);
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public static Tags getTagById(String id) {
        EntityManager em = getEntityManager();
        Tags m;

        Query q = em.createQuery("SELECT t FROM Tags t WHERE id = ?1", Tags.class);
        q.setParameter(1, id);
        m = (Tags) q.getSingleResult();

        em.close();

        return m;
    }

    public static int getPartnerAmount() {
        EntityManager em = getEntityManager();
        int size;

        Query q = em.createQuery("SELECT t FROM Tags t WHERE Partner = true", Tags.class);
        size = q.getResultList().size();

        em.close();

        return size;
    }

    public static void addUserTagsToDB(String id) {
        EntityManager em = getEntityManager();

        Tags t = new Tags();
        t.setId(id);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void giveTagToxic(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setToxic(true);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void removeTagToxic(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setToxic(false);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void giveTagPartner(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setPartner(true);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void removeTagPartner(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setPartner(false);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void giveTagVerified(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setVerified(true);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void removeTagVerified(String id) {
        EntityManager em = getEntityManager();

        Tags t = getTagById(id);
        t.setVerified(false);

        em.getTransaction().begin();
        em.merge(t);
        em.getTransaction().commit();

        em.close();
    }

    public static void saveMemberWaifu(Member m, User u) {
        EntityManager em = getEntityManager();

        m.setWaifu(u);

        em.getTransaction().begin();
        em.merge(m);
        em.getTransaction().commit();

        em.close();
    }

    @SuppressWarnings("unchecked")
    public static Exceed getExceed(ExceedEnums ex) {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT m FROM Member m WHERE exceed LIKE ?1", Member.class);
        q.setParameter(1, ex.getName());

        List<Member> members = (List<Member>) q.getResultList();

        return new Exceed(ex, members.size(), members.stream().mapToLong(Member::getXp).sum());
    }

    public static void markWinner(ExceedEnums ex) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();
        em.merge(new MonthWinner(ex, LocalDate.now().plusWeeks(1)));
        em.getTransaction().commit();

        em.close();
    }

    public static String getWinner() {
        EntityManager em = getEntityManager();

        Query q = em.createQuery("SELECT w FROM MonthWinner w WHERE id == MAX(id)", MonthWinner.class);
        try {
            MonthWinner winner = ((MonthWinner) q.getSingleResult());

            if (LocalDate.now().isBefore(winner.getExpiry())) {
                return winner.getExceed();
            } else {
                return "none";
            }
        } catch (NoResultException e) {
            return "none";
        }
    }
}
