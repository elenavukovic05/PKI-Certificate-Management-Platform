import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivateUserComponent } from './activate-user.component';

describe('ActivateUserComponent', () => {
  let component: ActivateUserComponent;
  let fixture: ComponentFixture<ActivateUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivateUserComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActivateUserComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
