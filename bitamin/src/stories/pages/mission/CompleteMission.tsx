import React, { useState, useEffect } from 'react';
import styles from '../../../styles/mission/quest2.module.css';
import { fetchMissionsByDate } from '@/api/missionAPI';
import MissionForm from './MissionForm';

interface Mission {
    id: number;
    missionName: string;
    missionDescription: string;
    missionLevel: number;
    completeDate: string;
    imageUrl: string;
    missionReview: string;
}

interface CompleteMissionProps {
    selectedDate: string;
}

const CompleteMission: React.FC<CompleteMissionProps> = ({ selectedDate }) => {
    const [mission, setMission] = useState<Mission | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const todayDate = new Date().toISOString().split('T')[0];

    const getMission = async (date: string) => {
        setLoading(true);
        try {
            const missionData = await fetchMissionsByDate(date);
            setMission(missionData);
        } catch (error) {
            console.error('Error fetching mission:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        setMission(null); // 상태 초기화
        getMission(selectedDate);
    }, [selectedDate]);

    const handleSubmitSuccess = () => {
        getMission(selectedDate);
    };

    return (
      <div className={styles.missionFormContainer}>
          {loading ? (
            <p>미션을 불러오는 중...</p>
          ) : mission ? (
            <>
                <div className={styles.todayMission}>
                    <h3>미션</h3>
                    <p>미션 이름: {mission.missionName}</p>
                    <p>미션 설명: {mission.missionDescription}</p>
                    <p>미션 레벨: {mission.missionLevel}</p>
                </div>
                <div className={styles.missionForm}>
                    <div>
                        <label htmlFor="missionReview">미션 리뷰:</label>
                        <input
                          id="missionReview"
                          type="text"
                          defaultValue={mission.missionReview}
                          required
                        />
                    </div>
                    <div>
                        <label htmlFor="missionImage">미션 이미지:</label>
                        <input
                          id="missionImage"
                          type="file"
                          accept="image/*"
                        />
                    </div>
                    {mission.imageUrl && (
                      <div>
                          <img
                            src={mission.imageUrl}
                            alt="Mission"
                            style={{ width: '300px', height: 'auto', marginTop: '10px' }}
                          />
                      </div>
                    )}
                </div>
            </>
          ) : (
            selectedDate === todayDate ? (
              <MissionForm selectedDate={selectedDate} onSubmitSuccess={handleSubmitSuccess} /> // 오늘 날짜에 미션이 없으면 MissionForm을 표시합니다.
            ) : (
              <p>해당 날짜에 완료된 미션이 없습니다.</p>
            )
          )}
      </div>
    );
};

export default CompleteMission;
